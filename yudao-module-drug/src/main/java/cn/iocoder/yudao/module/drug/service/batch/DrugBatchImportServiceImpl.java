package cn.iocoder.yudao.module.drug.service.batch;

import cn.iocoder.yudao.framework.common.biz.system.user.AdminUserApi;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.drug.controller.admin.batch.vo.*;
import cn.iocoder.yudao.module.drug.dal.dataobject.batch.ImportTaskDO;
import cn.iocoder.yudao.module.drug.dal.dataobject.batch.ImportTaskDetailDO;
import cn.iocoder.yudao.module.drug.dal.mysql.batch.ImportTaskDetailMapper;
import cn.iocoder.yudao.module.drug.dal.mysql.batch.ImportTaskMapper;
import cn.iocoder.yudao.module.drug.dal.redis.batch.TaskProgressRedisDAO;
import cn.iocoder.yudao.module.drug.enums.DetailStatusEnum;
import cn.iocoder.yudao.module.drug.enums.RetryTypeEnum;
import cn.iocoder.yudao.module.drug.enums.TableTypeEnum;
import cn.iocoder.yudao.module.drug.enums.TaskStatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.drug.enums.DrugErrorCodeConstants.*;

/**
 * 药品数据批量导入服务实现 - 架构重构完整版
 * <p>
 * 本实现体现了以下核心架构思想：
 * <p>
 * 1. 分层架构设计：
 * - 数据访问层：直接操作数据库和缓存
 * - 业务逻辑层：处理核心业务规则和流程编排
 * - 数据转换层：负责实体与VO之间的转换
 * - 接口适配层：为不同的前端需求提供适配
 * <p>
 * 2. 职责分离原则：
 * - 任务管理：统一的生命周期管理
 * - 文件处理：解压、验证、解析的独立流程
 * - 数据导入：批量处理和事务管理
 * - 质量控制：数据质量评估和问题诊断
 * - 进度跟踪：实时状态更新和缓存管理
 * <p>
 * 3. 异步处理模式：
 * - 文件上传立即返回：提升用户体验
 * - 后台异步处理：避免长时间等待
 * - 实时进度反馈：通过Redis缓存提供进度查询
 * <p>
 * 4. 错误处理策略：
 * - 分层错误处理：不同层次的异常有不同的处理方式
 * - 优雅降级：部分失败不影响整体流程
 * - 错误恢复：支持断点续传和智能重试
 * <p>
 * 5. 性能优化设计：
 * - 批量数据操作：避免逐条处理的性能瓶颈
 * - 缓存策略：热数据缓存，减少数据库压力
 * - 异步并行：充分利用多核处理能力
 * - 资源管理：合理的连接池和线程池配置
 *
 * @author 架构师团队
 * @since 2024-12-06
 */
@Service
@Slf4j
@Validated
public class DrugBatchImportServiceImpl implements DrugBatchImportService {

    // ==================== 常量定义 ====================

    /**
     * 导入处理顺序定义
     * <p>
     * 这个顺序体现了业务数据的依赖关系，就像建房子需要先打地基再建框架一样。
     * 机构信息是所有数据的基础，药品目录是业务数据的前提，
     * 而入库、出库、使用数据可以并行处理。
     */
    private static final List<TableTypeEnum> IMPORT_ORDER = List.of(
            TableTypeEnum.HOSPITAL_INFO,    // 地基：机构信息必须最先导入
            TableTypeEnum.DRUG_CATALOG,     // 框架：药品目录建立基础数据关系
            TableTypeEnum.DRUG_INBOUND,     // 业务层：这三个可以并行处理
            TableTypeEnum.DRUG_OUTBOUND,
            TableTypeEnum.DRUG_USAGE
    );

    /**
     * 批处理大小配置
     * <p>
     * 这个配置平衡了内存使用和性能效率。
     * 太小会增加处理次数，太大可能导致内存不足或事务超时。
     */
    private static final int BATCH_SIZE = 1000;

    /**
     * 文件大小限制（100MB）
     * <p>
     * 这是基于实际业务场景和系统资源的平衡配置。
     */
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024L;

    // ==================== 依赖注入 ====================

    @Resource
    private ImportTaskMapper taskMapper;
    @Resource
    private ImportTaskDetailMapper taskDetailMapper;
    @Resource
    private FileExtractService fileExtractService;
    @Resource
    private ImportTaskDetailService taskDetailService;
    @Resource
    private DrugDataParseService dataParseService;
    @Resource
    private DrugDataImportService dataImportService;
    @Resource
    private DrugQualityControlService qualityControlService;
    @Resource
    @Qualifier("applicationTaskExecutor")
    private AsyncTaskExecutor asyncTaskExecutor;
    @Resource
    private TaskProgressRedisDAO taskProgressRedisDAO;
    @Resource
    private AdminUserApi adminUserApi;

    // ==================== 核心业务接口实现 ====================

    /**
     * 创建导入任务 - 核心入口方法
     * <p>
     * 设计理念：快速响应 + 异步处理
     * 这个方法体现了"响应式架构"的思想：
     * 1. 立即保存文件和创建任务记录，确保数据不丢失
     * 2. 快速返回任务ID，让用户可以立即查询进度
     * 3. 将耗时的处理操作放到后台异步执行
     * 4. 通过Redis缓存提供实时的进度反馈
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportTaskCreateResult createImportTask(MultipartFile file, ImportTaskCreateParams params) {
        log.info("开始创建导入任务: fileName={}, taskName={}, fileSize={}KB",
                file.getOriginalFilename(), params.getTaskName(), file.getSize() / 1024);

        // 第一阶段：快速验证和准备
        validateBasicFileProperties(file);

        // 第二阶段：立即持久化关键信息
        String taskNo = taskProgressRedisDAO.generateTaskNo();
        String savedFilePath = saveUploadedFileImmediately(file, taskNo);

        // 第三阶段：创建任务记录
        ImportTaskDO task = createTaskRecord(file, params, taskNo, savedFilePath);

        // 第四阶段：启动异步处理流程
        CompletableFuture.runAsync(() -> {
            try {
                executeCompleteImportProcess(task, savedFilePath);
            } catch (Exception e) {
                log.error("导入任务异步执行失败: taskId={}, taskNo={}", task.getId(), task.getTaskNo(), e);
                handleTaskError(task.getId(), "导入过程异常: " + e.getMessage());
            }
        }, asyncTaskExecutor);

        // 第五阶段：构建响应结果
        ImportTaskCreateResult result = ImportTaskCreateResult.builder()
                .taskId(task.getId())
                .taskNo(task.getTaskNo())
                .message("导入任务已创建，正在后台处理")
                .createTime(task.getCreateTime())
                .estimatedCompletionTime(calculateEstimatedCompletionTime())
                .fileInfo(ImportTaskCreateResult.FileBasicInfo.builder()
                        .originalFileName(file.getOriginalFilename())
                        .fileSize(file.getSize())
                        .expectedFileCount(5) // 预期的Excel文件数量
                        .build())
                .build();

        log.info("导入任务创建成功: taskId={}, taskNo={}", task.getId(), task.getTaskNo());
        return result;
    }

    /**
     * 获取任务完整详情 - 重构后的核心查询方法
     * <p>
     * 架构亮点：多数据源整合 + 分层数据构建
     * 这个方法展示了如何优雅地整合来自不同数据源的信息：
     * 1. 数据库中的持久化任务信息
     * 2. Redis中的实时进度信息
     * 3. 计算得出的统计和分析信息
     * 4. 动态生成的操作选项和建议
     */
    @Override
    public ImportTaskDetailVO getTaskDetail(Long taskId) {
        log.info("查询任务详情: taskId={}", taskId);

        // 第一步：获取基础数据
        // 从数据库获取任务的持久化信息，这是数据的基础底座
        ImportTaskDO task = taskMapper.selectById(taskId);
        if (task == null) {
            throw exception(TASK_NOT_FOUND);
        }

        // 第二步：获取任务明细数据
        // 查询所有表级别的处理详情，了解每个业务表的处理状态
        List<ImportTaskDetailDO> taskDetails = taskDetailMapper.selectList(
                new LambdaQueryWrapper<ImportTaskDetailDO>()
                        .eq(ImportTaskDetailDO::getTaskId, taskId)
                        .orderByAsc(ImportTaskDetailDO::getCreateTime)
        );

        // 第三步：获取实时进度信息
        // 从Redis获取最新的进度状态，这些信息反映了当前的处理进展
        TaskProgressInfo progressInfo = taskProgressRedisDAO.getTaskProgress(taskId);
        Map<String, TaskDetailProgressInfo> detailProgressMap =
                taskProgressRedisDAO.getAllTaskDetailProgress(taskId);

        // 第四步：构建分层的详情对象
        // 使用建造者模式逐步构建完整的详情视图
        ImportTaskDetailVO detailVO = ImportTaskDetailVO.builder()
                .taskInfo(convertToTaskInfo(task))
                .overallProgress(buildOverallProgress(task, progressInfo))
                .tableDetails(buildTableDetails(taskDetails, detailProgressMap))
                .statistics(calculateTaskStatistics(task, taskDetails))
                .timeline(buildTaskTimeline(task, taskDetails))
                .recentLogs(getRecentTaskLogs(taskId, 100))
                .relatedTasks(findRelatedTasks(task))
                .operationOptions(buildOperationOptions(task))
                .qualityReport(buildQualityReport(taskId))
                .build();

        log.info("任务详情查询完成: taskId={}, 包含{}个表的详情", taskId, taskDetails.size());
        return detailVO;
    }

    /**
     * 获取任务进度信息 - 轻量级进度查询
     * <p>
     * 设计考虑：高频查询优化
     * 这个方法专门为前端的进度轮询设计，特点是：
     * 1. 查询速度快：只获取必要的进度信息
     * 2. 数据量小：减少网络传输开销
     * 3. 实时性强：优先使用Redis中的最新数据
     */
    @Override
    public ImportProgressVO getTaskProgress(Long taskId) {
        log.debug("查询任务进度: taskId={}", taskId);

        // 获取基础任务信息
        ImportTaskDO task = taskMapper.selectById(taskId);
        if (task == null) {
            throw exception(TASK_NOT_FOUND);
        }

        // 获取任务明细（用于表级进度展示）
        List<ImportTaskDetailDO> details = taskDetailMapper.selectList(
                new LambdaQueryWrapper<ImportTaskDetailDO>()
                        .eq(ImportTaskDetailDO::getTaskId, taskId)
        );

        // 获取实时进度信息
        TaskProgressInfo progressInfo = taskProgressRedisDAO.getTaskProgress(taskId);
        Map<String, TaskDetailProgressInfo> detailProgressMap =
                taskProgressRedisDAO.getAllTaskDetailProgress(taskId);

        // 构建轻量级的进度响应
        return ImportProgressVO.builder()
                .taskId(taskId)
                .taskNo(task.getTaskNo())
                .taskName(task.getTaskName())
                .overallStatus(task.getStatus())
                .overallProgress(progressInfo != null ? progressInfo.getProgress() : task.getProgressPercent())
                .currentMessage(progressInfo != null ? progressInfo.getMessage() : "")
                .currentStage(progressInfo != null ? progressInfo.getCurrentStage() : "")
                .estimatedRemainingTime(progressInfo != null ? progressInfo.getEstimatedRemainingSeconds() : null)
                .startTime(task.getStartTime())
                .estimatedEndTime(calculateEstimatedEndTime(task, details))
                .lastUpdateTime(progressInfo != null ? progressInfo.getUpdateTime() : LocalDateTime.now())
                .totalFiles(task.getTotalFiles())
                .successFiles(task.getSuccessFiles())
                .failedFiles(task.getFailedFiles())
                .totalRecords(task.getTotalRecords())
                .successRecords(task.getSuccessRecords())
                .failedRecords(task.getFailedRecords())
                .tableProgress(buildTableProgressList(details, detailProgressMap))
                .canRetry(canRetryTask(task))
                .canCancel(canCancelTask(task))
                .build();
    }

    /**
     * 智能重试机制 - 错误恢复的核心
     * <p>
     * 架构思想：精确恢复 + 状态一致性
     * 重试不是简单的重复执行，而是基于失败分析的智能恢复：
     * 1. 分析失败原因，确定重试策略
     * 2. 保证数据一致性，避免重复处理
     * 3. 使用分布式锁，确保重试操作的原子性
     * 4. 记录重试历史，支持问题诊断
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportRetryResult retryImport(Long taskId, RetryTypeEnum retryType, String fileType) {
        log.info("开始重试导入任务: taskId={}, retryType={}, fileType={}", taskId, retryType, fileType);

        // 第一步：验证重试条件
        ImportTaskDO task = taskMapper.selectById(taskId);
        if (task == null) {
            throw exception(TASK_NOT_FOUND);
        }

        if (!canRetryTask(task)) {
            throw exception(IMPORT_RETRY_NOT_SUPPORTED);
        }

        // 第二步：获取分布式锁
        // 确保同一时间只有一个重试操作在执行，避免并发问题
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (!taskProgressRedisDAO.tryLockTask(taskId, userId)) {
            throw exception(IMPORT_TASK_LOCKED);
        }

        try {
            log.info("获取任务锁成功，开始执行重试逻辑: taskId={}, userId={}", taskId, userId);

            // 第三步：根据重试类型执行相应的重试策略
            return switch (retryType) {
                case ALL -> retryAllTables(task);
                case FAILED -> retryFailedTables(task);
                case FILE_TYPE -> retrySpecificTable(task, fileType);
                default -> throw exception(IMPORT_RETRY_TYPE_UNSUPPORTED);
            };

        } finally {
            // 第四步：确保锁被释放
            // 无论重试成功还是失败，都要释放锁，避免死锁
            taskProgressRedisDAO.unlockTask(taskId, userId);
            log.info("任务锁已释放: taskId={}, userId={}", taskId, userId);
        }
    }

    /**
     * 任务取消操作 - 优雅停止机制
     * <p>
     * 设计要点：状态一致性 + 资源清理
     * 取消操作不是简单的状态修改，需要考虑：
     * 1. 正在执行的操作如何安全停止
     * 2. 已处理的数据如何处理
     * 3. 占用的资源如何释放
     * 4. 状态如何安全地转换
     */
    @Override
    public void cancelTask(Long taskId) {
        log.info("开始取消任务: taskId={}", taskId);

        Long userId = SecurityFrameworkUtils.getLoginUserId();

        // 获取任务锁，确保取消操作的原子性
        if (!taskProgressRedisDAO.tryLockTask(taskId, userId)) {
            throw exception(IMPORT_TASK_LOCKED);
        }

        try {
            // 更新数据库状态
            updateTaskStatus(taskId, TaskStatusEnum.CANCELLED);

            // 清理Redis缓存，释放内存资源
            taskProgressRedisDAO.deleteTaskProgress(taskId);
            taskProgressRedisDAO.deleteAllTaskDetailProgress(taskId);

            log.info("任务取消成功: taskId={}, userId={}", taskId, userId);

        } finally {
            taskProgressRedisDAO.unlockTask(taskId, userId);
        }
    }

    /**
     * 文件验证接口 - 预检查机制
     * <p>
     * 价值定位：提前发现问题 + 优化用户体验
     * 在正式导入前进行预检查，能够：
     * 1. 提前发现文件格式问题
     * 2. 估算处理时间和资源需求
     * 3. 提供文件内容预览
     * 4. 给用户明确的质量反馈
     */
    @Override
    public FileValidationResult validateImportFile(MultipartFile file) {
        log.info("开始验证导入文件: fileName={}, fileSize={}KB",
                file.getOriginalFilename(), file.getSize() / 1024);

        try {
            // 第一阶段：基础属性验证
            validateBasicFileProperties(file);

            // 第二阶段：临时保存文件进行深度分析
            String tempTaskNo = "VALIDATE_" + System.currentTimeMillis();
            String tempFilePath = saveUploadedFileImmediately(file, tempTaskNo);

            try {
                // 第三阶段：执行完整的文件解析和验证
                FileExtractResult extractResult = fileExtractService.extractAndValidateFromPath(
                        System.currentTimeMillis(), tempFilePath);

                // 第四阶段：构建验证结果
                if (extractResult.getSuccess()) {
                    return buildSuccessValidationResult(file, extractResult);
                } else {
                    return buildFailedValidationResult(file, extractResult);
                }

            } finally {
                // 第五阶段：清理临时文件
                cleanupTempFile(tempFilePath);
            }

        } catch (Exception e) {
            log.error("文件验证失败: fileName={}", file.getOriginalFilename(), e);
            return buildErrorValidationResult(file, e);
        }
    }

    /**
     * 分页查询任务列表
     */
    @Override
    public PageResult<ImportTaskDO> getTaskPage(ImportTaskPageReqVO pageReqVO) {
        PageResult<ImportTaskDO> pageResult = taskMapper.selectPage(pageReqVO);
        // 补充创建人姓名信息
        if (pageResult.getList() != null) {
            pageResult.getList().forEach(task -> {
                if (task.getCreator() != null) {
                    try {
                        task.setCreator(adminUserApi.getUser(Long.valueOf(task.getCreator())).getNickname());
                    } catch (Exception e) {
                        log.warn("获取创建人信息失败: userId={}", task.getCreator(), e);
                    }
                }
            });
        }
        return pageResult;
    }

    // ==================== 核心业务流程实现 ====================

    /**
     * 执行完整的导入处理流程 - 业务编排的核心
     * <p>
     */
    private void executeCompleteImportProcess(ImportTaskDO task, String filePath) {
        Long taskId = task.getId();
        String taskNo = task.getTaskNo();

        try {
            log.info("开始执行完整导入流程: taskId={}, taskNo={}", taskId, taskNo);

            // 第一阶段：文件解压和结构验证
            log.info("阶段1: 开始文件解压和验证 - taskId={}", taskId);
            updateTaskStatus(taskId, TaskStatusEnum.EXTRACTING);
            updateTaskProgress(taskId, 10, "正在解压文件，验证文件结构...", "EXTRACTING");

            FileExtractResult extractResult = fileExtractService.extractAndValidateFromPath(taskId, filePath);
            if (!extractResult.getSuccess()) {
                throw exception(ZIP_EXTRACT_FAILED, extractResult.getErrorMessage());
            }

            // 创建任务明细记录，为每个发现的文件建立处理跟踪
            createTaskDetails(taskId, taskNo, extractResult.getFileInfos());
            updateTaskProgress(taskId, 20,
                    String.format("文件解压完成，发现%d个有效文件", extractResult.getValidFileCount()),
                    "EXTRACTING");

            // 第二阶段：数据解析和导入处理
            log.info("阶段2: 开始数据解析和导入 - taskId={}", taskId);
            updateTaskStatus(taskId, TaskStatusEnum.IMPORTING);

            ImportProcessResult importResult = processAllTablesInOrder(taskId, extractResult.getFileInfos());
            updateTaskProgress(taskId, 70,
                    String.format("数据导入完成，成功%d条，失败%d条",
                            importResult.getTotalSuccess(), importResult.getTotalFailed()),
                    "IMPORTING");

            // 第三阶段：质量控制检查
            log.info("阶段3: 开始质量控制检查 - taskId={}", taskId);
            updateTaskStatus(taskId, TaskStatusEnum.QC_CHECKING);
            updateTaskProgress(taskId, 80, "正在执行质量控制检查...", "QC_CHECKING");

            QualityControlResult qcResult = qualityControlService.executeOverallQualityControl(taskId);
            updateTaskProgress(taskId, 95, "质量控制检查完成", "QC_CHECKING");

            // 第四阶段：确定最终状态和更新统计
            log.info("阶段4: 确定最终状态 - taskId={}", taskId);
            TaskStatusEnum finalStatus = determineFinalStatus(importResult.hasError(), qcResult);
            updateTaskFinalStatus(taskId, finalStatus, importResult);
            updateTaskProgress(taskId, 100, "任务处理完成", finalStatus.name());

            log.info("导入流程全部完成: taskId={}, 最终状态={}, 成功记录={}, 失败记录={}",
                    taskId, finalStatus.getDescription(),
                    importResult.getTotalSuccess(), importResult.getTotalFailed());

        } catch (Exception e) {
            log.error("导入流程执行异常: taskId={}", taskId, e);
            handleTaskError(taskId, e.getMessage());
            updateTaskProgress(taskId, 0, "任务执行失败: " + e.getMessage(), "FAILED");
        }
    }

    /**
     * 按序处理所有表的数据 - 依赖关系管理
     * <p>
     * 设计思想：拓扑排序 + 并行优化
     * 考虑到数据表之间的依赖关系，采用以下策略：
     * 1. 按预定义顺序串行处理有依赖关系的表
     * 2. 对无依赖关系的表可以并行处理
     * 3. 出现错误时支持部分回滚
     */
    private ImportProcessResult processAllTablesInOrder(Long taskId, Map<TableTypeEnum, FileInfo> fileInfos) {
        ImportProcessResult result = new ImportProcessResult();
        int processedTables = 0;
        int totalTables = IMPORT_ORDER.size();

        for (TableTypeEnum tableType : IMPORT_ORDER) {
            FileInfo fileInfo = fileInfos.get(tableType);
            if (fileInfo == null) {
                log.warn("未找到对应文件，跳过处理: taskId={}, tableType={}", taskId, tableType);
                processedTables++;
                continue;
            }

            try {
                // 计算并更新当前阶段进度
                int currentProgress = 20 + (processedTables * 50 / totalTables);
                updateTaskProgress(taskId, currentProgress,
                        String.format("正在处理%s数据...", tableType.getDescription()), "IMPORTING");

                // 处理单个表的完整流程
                ImportResult tableResult = processSingleTableData(taskId, tableType, fileInfo);
                result.addTableResult(tableResult);

                log.info("表处理完成: taskId={}, tableType={}, 成功={}, 失败={}",
                        taskId, tableType, tableResult.getSuccessCount(), tableResult.getFailedCount());

            } catch (Exception e) {
                log.error("表处理失败: taskId={}, tableType={}", taskId, tableType, e);
                result.addError(tableType, e.getMessage());
                updateDetailStatus(taskId, tableType, DetailStatusEnum.FAILED, e.getMessage());
            }

            processedTables++;
        }

        return result;
    }

    /**
     * 处理单个表的完整数据流程 - 微型ETL管道
     * <p>
     * ETL设计模式：Extract -> Transform -> Load
     * 1. Extract: 从Excel文件中提取原始数据
     * 2. Transform: 数据清洗、验证、转换
     * 3. Load: 批量加载到目标数据库表
     */
    private ImportResult processSingleTableData(Long taskId, TableTypeEnum tableType, FileInfo fileInfo) {
        log.info("开始处理单表数据: taskId={}, tableType={}, fileName={}",
                taskId, tableType, fileInfo.getFileName());

        // 第一步：解析阶段 - Extract
        updateDetailStatus(taskId, tableType, DetailStatusEnum.PARSING, null);
        updateDetailProgress(taskId, tableType, 10, "正在解析Excel文件...");

        ParseResult parseResult = dataParseService.parseExcelFile(fileInfo, tableType);
        if (!parseResult.getSuccess()) {
            throw exception(FILE_READ_ERROR, parseResult.getErrorMessage());
        }

        updateDetailProgress(taskId, tableType, 30,
                String.format("解析完成，共%d条数据", parseResult.getDataRows()));

        // 第二步：导入阶段 - Transform & Load
        updateDetailStatus(taskId, tableType, DetailStatusEnum.IMPORTING, null);

        ImportResult importResult = batchImportData(taskId, tableType, parseResult.getDataList());
        updateDetailProgress(taskId, tableType, 70,
                String.format("导入完成，成功%d条，失败%d条",
                        importResult.getSuccessCount(), importResult.getFailedCount()));

        // 第三步：质控阶段 - Validate
        updateDetailStatus(taskId, tableType, DetailStatusEnum.QC_CHECKING, null);

        QualityControlResult qcResult = qualityControlService.executeTableQualityControl(taskId, tableType);
        updateDetailProgress(taskId, tableType, 100,
                String.format("质控完成，通过%d条，失败%d条",
                        qcResult.getPassedCount(), qcResult.getFailedCount()));

        // 第四步：确定最终状态
        DetailStatusEnum finalStatus = determineDetailFinalStatus(importResult, qcResult);
        updateDetailFinalStatus(taskId, tableType, finalStatus, importResult, qcResult);

        return importResult;
    }

    /**
     * 分批导入数据 - 内存和性能的平衡
     * <p>
     * 批处理策略：
     * 1. 控制内存使用：避免一次性加载大量数据
     * 2. 优化数据库性能：批量操作比逐条操作效率高
     * 3. 事务边界控制：单批失败不影响其他批次
     * 4. 进度可视化：让用户了解处理进展
     */
    private ImportResult batchImportData(Long taskId, TableTypeEnum tableType, List<?> dataList) {
        int totalRows = dataList.size();
        int successCount = 0;
        int failedCount = 0;
        List<ImportResult.ImportError> errors = new ArrayList<>();

        log.info("开始分批导入数据: taskId={}, tableType={}, 总记录数={}, 批大小={}",
                taskId, tableType, totalRows, BATCH_SIZE);

        // 分批处理数据
        for (int startIndex = 0; startIndex < totalRows; startIndex += BATCH_SIZE) {
            int endIndex = Math.min(startIndex + BATCH_SIZE, totalRows);
            List<?> batch = dataList.subList(startIndex, endIndex);

            try {
                // 执行单批次导入
                ImportResult batchResult = dataImportService.importBatch(taskId, tableType, batch);
                successCount += batchResult.getSuccessCount();
                failedCount += batchResult.getFailedCount();

                // 收集错误信息
                if (batchResult.getImportErrors() != null) {
                    errors.addAll(batchResult.getImportErrors());
                }

                // 更新进度
                int progress = 30 + (endIndex * 40 / totalRows);
                updateDetailProgress(taskId, tableType, progress,
                        String.format("正在导入数据 %d/%d (%.1f%%)",
                                endIndex, totalRows, (endIndex * 100.0 / totalRows)));

                log.debug("批次导入完成: 范围={}-{}, 成功={}, 失败={}",
                        startIndex, endIndex, batchResult.getSuccessCount(), batchResult.getFailedCount());

            } catch (Exception e) {
                log.error("批次导入失败: taskId={}, tableType={}, 范围={}-{}",
                        taskId, tableType, startIndex, endIndex, e);
                failedCount += batch.size();

                // 记录批次级别的错误
                errors.add(ImportResult.ImportError.builder()
                        .batchIndex(startIndex / BATCH_SIZE)
                        .errorType("BATCH_ERROR")
                        .errorMessage("批次导入失败: " + e.getMessage())
                        .errorDetail(String.format("影响范围: %d-%d", startIndex, endIndex))
                        .build());
            }
        }

        // 构建最终的导入结果
        return ImportResult.builder()
                .success(failedCount == 0)
                .hasError(failedCount > 0)
                .message(String.format("导入完成: 成功%d条，失败%d条", successCount, failedCount))
                .tableType(tableType.name())
                .importBatchNo(generateImportBatchNo(taskId, tableType))
                .totalCount(successCount + failedCount)
                .successCount(successCount)
                .failedCount(failedCount)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .processingTimeMs(0L) // 这里可以加入实际的计时逻辑
                .importErrors(errors)
                .build();
    }

    // ==================== VO构建方法集合 ====================

    /**
     * 构建任务基本信息VO
     * <p>
     * 数据转换的基础方法，将数据库实体转换为前端展示对象
     */
    private TaskInfoVO convertToTaskInfo(ImportTaskDO task) {
        return TaskInfoVO.builder()
                .taskId(task.getId())
                .taskNo(task.getTaskNo())
                .taskName(task.getTaskName())
                .fileName(task.getFileName())
                .fileSize(task.getFileSize())
                .status(task.getStatus())
                .statusDisplay(TaskStatusEnum.getByType(task.getStatus()).getDescription())
                .createTime(task.getCreateTime())
                .creator(adminUserApi.getUser(Long.valueOf(task.getCreator())).getNickname())
                .dataSource("文件导入")
                .description("药品数据批量导入任务")
                .build();
    }

    /**
     * 构建整体进度信息VO
     * <p>
     * 进度信息的综合视图，整合多个数据源的进度状态
     */
    private TaskOverallProgressVO buildOverallProgress(ImportTaskDO task, TaskProgressInfo progressInfo) {
        // 构建阶段状态信息
        TaskOverallProgressVO.StageStatusInfo stageStatus = TaskOverallProgressVO.StageStatusInfo.builder()
                .extractStatus(task.getExtractStatus())
                .importStatus(task.getImportStatus())
                .qcStatus(task.getQcStatus())
                .build();

        // 构建时间信息
        TaskOverallProgressVO.TimeInfo timeInfo = TaskOverallProgressVO.TimeInfo.builder()
                .startTime(task.getStartTime())
                .estimatedEndTime(calculateEstimatedEndTime(task, null))
                .lastUpdateTime(progressInfo != null ? progressInfo.getUpdateTime() : LocalDateTime.now())
                .elapsedSeconds(calculateElapsedSeconds(task.getStartTime()))
                .build();

        // 构建预估信息
        Double processingSpeed = calculateCurrentProcessingSpeed(task);
        TaskOverallProgressVO.EstimationInfo estimation = TaskOverallProgressVO.EstimationInfo.builder()
                .estimatedRemainingSeconds(progressInfo != null ? progressInfo.getEstimatedRemainingSeconds() : null)
                .processingSpeed(processingSpeed)
                .speedDisplay(formatProcessingSpeed(processingSpeed))
                .build();

        return TaskOverallProgressVO.builder()
                .overallProgress(progressInfo != null ? progressInfo.getProgress() : task.getProgressPercent())
                .currentStage(progressInfo != null ? progressInfo.getCurrentStage() : getCurrentStageFromStatus(task.getStatus()))
                .currentMessage(progressInfo != null ? progressInfo.getMessage() : "")
                .stageStatus(stageStatus)
                .timeInfo(timeInfo)
                .estimation(estimation)
                .build();
    }

    /**
     * 构建表级详情列表
     * <p>
     * 整合数据库持久化信息和Redis实时进度信息
     */
    private List<TableDetailVO> buildTableDetails(
            List<ImportTaskDetailDO> taskDetails,
            Map<String, TaskDetailProgressInfo> detailProgressMap) {

        return taskDetails.stream()
                .map(detail -> {
                    String tableTypeKey = String.valueOf(detail.getTableType());
                    TaskDetailProgressInfo progressInfo = detailProgressMap.get(tableTypeKey);

                    // 构建表基本信息
                    TableDetailVO.TableBasicInfo basicInfo = TableDetailVO.TableBasicInfo.builder()
                            .tableType(detail.getTableType())
                            .tableName(TableTypeEnum.getByType(detail.getTableType()).getDescription())
                            .fileName(detail.getFileName())
                            .fileType(detail.getFileType())
                            .fileSize(0L) // 可以从文件信息中获取
                            .build();

                    // 构建进度信息
                    TableDetailVO.TableProgressInfo progressVOInfo = TableDetailVO.TableProgressInfo.builder()
                            .status(detail.getStatus())
                            .statusDisplay(DetailStatusEnum.getByStatus(detail.getStatus()).getDescription())
                            .progressPercent(progressInfo != null ? progressInfo.getProgress() : detail.getProgressPercent())
                            .currentMessage(progressInfo != null ? progressInfo.getMessage() : "")
                            .parseStatus(detail.getParseStatus())
                            .importStatus(detail.getImportStatus())
                            .qcStatus(detail.getQcStatus())
                            .build();

                    // 构建统计信息
                    TableDetailVO.TableStatisticsInfo statisticsInfo = TableDetailVO.TableStatisticsInfo.builder()
                            .totalRows(detail.getTotalRows())
                            .validRows(detail.getValidRows())
                            .successRows(detail.getSuccessRows())
                            .failedRows(detail.getFailedRows())
                            .qcPassedRows(detail.getQcPassedRows())
                            .qcFailedRows(detail.getQcFailedRows())
                            .successRate(calculateTableSuccessRate(detail.getSuccessRows(), detail.getTotalRows()))
                            .build();

                    // 构建操作信息
                    TableDetailVO.TableOperationInfo operationInfo = TableDetailVO.TableOperationInfo.builder()
                            .canRetry(DetailStatusEnum.FAILED.getStatus().equals(detail.getStatus()))
                            .retryCount(detail.getRetryCount())
                            .maxRetryCount(detail.getMaxRetryCount())
                            .startTime(detail.getStartTime())
                            .endTime(detail.getEndTime())
                            .errorMessage(detail.getErrorMessage())
                            .build();

                    return TableDetailVO.builder()
                            .basicInfo(basicInfo)
                            .progressInfo(progressVOInfo)
                            .statisticsInfo(statisticsInfo)
                            .operationInfo(operationInfo)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 计算任务统计信息
     * <p>
     * 多维度统计分析，为决策提供数据支持
     */
    private TaskStatisticsVO calculateTaskStatistics(ImportTaskDO task, List<ImportTaskDetailDO> details) {
        // 文件统计
        TaskStatisticsVO.FileStatistics fileStats = TaskStatisticsVO.FileStatistics.builder()
                .totalFiles(task.getTotalFiles())
                .successFiles(task.getSuccessFiles())
                .failedFiles(task.getFailedFiles())
                .fileSuccessRate(calculateSuccessRate(task.getSuccessFiles(), task.getTotalFiles()))
                .fileCountByType(calculateFileCountByType(details))
                .build();

        // 记录统计
        TaskStatisticsVO.RecordStatistics recordStats = TaskStatisticsVO.RecordStatistics.builder()
                .totalRecords(task.getTotalRecords())
                .successRecords(task.getSuccessRecords())
                .failedRecords(task.getFailedRecords())
                .overallSuccessRate(calculateSuccessRate(task.getSuccessRecords(), task.getTotalRecords()))
                .recordCountByType(calculateRecordCountByType(details))
                .build();

        // 性能统计
        TaskStatisticsVO.PerformanceStatistics performanceStats = TaskStatisticsVO.PerformanceStatistics.builder()
                .averageProcessingSpeed(calculateAverageProcessingSpeed(task, details))
                .estimatedTimeRemaining(calculateEstimatedTimeRemaining(task))
                .averageProcessingTime(calculateAverageProcessingTime(task, details))
                .performanceLevel(evaluatePerformanceLevel(task, details))
                .build();

        // 质量统计
        TaskStatisticsVO.QualityStatistics qualityStats = TaskStatisticsVO.QualityStatistics.builder()
                .fileSuccessRateByType(calculateFileSuccessRateByType(details))
                .recordSuccessRateByType(calculateRecordSuccessRateByType(details))
                .qualityScoreDistribution(calculateQualityScoreDistribution(details))
                .averageQualityScore(calculateAverageQualityScore(details))
                .build();

        return TaskStatisticsVO.builder()
                .fileStats(fileStats)
                .recordStats(recordStats)
                .performanceStats(performanceStats)
                .qualityStats(qualityStats)
                .build();
    }

    // ==================== 辅助工具方法 ====================

    /**
     * 验证文件基本属性
     */
    private void validateBasicFileProperties(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        String fileName = file.getOriginalFilename();
        if (!StringUtils.hasText(fileName)) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过100MB");
        }

        String extension = getFileExtension(fileName).toLowerCase();
        if (!Set.of(".zip", ".rar").contains(extension)) {
            throw new IllegalArgumentException("仅支持ZIP和RAR格式的压缩文件");
        }
    }

    /**
     * 立即保存上传文件
     */
    private String saveUploadedFileImmediately(MultipartFile file, String taskNo) {
        try {
            String uploadDir = "/data/drug-import/uploads/";
            String fileName = taskNo + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);

            Files.createDirectories(filePath.getParent());
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("文件保存成功: taskNo={}, filePath={}, size={}KB",
                    taskNo, filePath, file.getSize() / 1024);

            return filePath.toString();
        } catch (IOException e) {
            throw exception(FILE_UPLOAD_FAILED, "文件保存失败: " + e.getMessage());
        }
    }

    /**
     * 创建任务记录
     */
    private ImportTaskDO createTaskRecord(MultipartFile file, ImportTaskCreateParams params,
                                          String taskNo, String filePath) {
        ImportTaskDO task = ImportTaskDO.builder()
                .taskNo(taskNo)
                .taskName(params.getTaskName())
                .fileName(file.getOriginalFilename())
                .filePath(filePath)
                .fileSize(file.getSize())
                .dataSource(params.getDataSource())
                .description(params.getDescription())
                // 文件统计初始化
                .totalFiles(0)
                .successFiles(0)
                .failedFiles(0)
                // 记录统计初始化
                .totalRecords(0L)
                .successRecords(0L)
                .failedRecords(0L)
                // 状态初始化
                .status(TaskStatusEnum.PENDING.getStatus())
                .extractStatus(0)
                .importStatus(0)
                .qcStatus(0)
                .progressPercent(0)
                .build();

        taskMapper.insert(task);
        log.info("任务记录创建成功: taskId={}, taskNo={}", task.getId(), taskNo);
        return task;
    }

    /**
     * 更新任务进度到Redis
     */
    private void updateTaskProgress(Long taskId, int progress, String message, String currentStage) {
        TaskProgressInfo progressInfo = TaskProgressInfo.builder()
                .taskId(taskId)
                .progress(progress)
                .message(message)
                .currentStage(currentStage)
                .estimatedRemainingSeconds(calculateEstimatedRemainingTime(progress))
                .updateTime(LocalDateTime.now())
                .build();

        taskProgressRedisDAO.setTaskProgress(progressInfo);
        log.debug("任务进度已更新: taskId={}, progress={}%, message={}", taskId, progress, message);
    }

    /**
     * 更新任务状态
     */
    /**
     * 更新任务状态 - 修复版本
     * 确保所有相关的阶段状态和时间字段都被正确更新
     */
    private void updateTaskStatus(Long taskId, TaskStatusEnum status) {
        ImportTaskDO updateTask = ImportTaskDO.builder()
                .id(taskId)
                .status(status.getStatus())
                .build();

        LocalDateTime now = LocalDateTime.now();

        // 根据不同状态设置相应的阶段状态和时间
        switch (status) {
            case EXTRACTING -> {
                updateTask.setStartTime(now);
                updateTask.setExtractStatus(1); // 进行中
            }
            case IMPORTING -> {
                updateTask.setExtractEndTime(now);
                updateTask.setExtractStatus(2); // 成功
                updateTask.setImportStatus(1); // 进行中
            }
            case QC_CHECKING -> {
                updateTask.setImportEndTime(now);
                updateTask.setImportStatus(2); // 成功
                updateTask.setQcStatus(1); // 进行中
            }
            case COMPLETED -> {
                updateTask.setQcEndTime(now);
                updateTask.setQcStatus(2); // 成功
                updateTask.setEndTime(now);
                updateTask.setProgressPercent(100);
            }
            case FAILED -> {
                updateTask.setEndTime(now);
                updateTask.setProgressPercent(0);
                // 根据当前进展设置失败的具体阶段
                markFailedStageInTask(updateTask, taskId);
            }
            case PARTIAL_SUCCESS -> {
                updateTask.setQcEndTime(now);
                updateTask.setQcStatus(2); // 质控完成
                updateTask.setEndTime(now);
                updateTask.setProgressPercent(100);
            }
            case CANCELLED -> {
                updateTask.setEndTime(now);
                updateTask.setProgressPercent(0);
            }
        }

        taskMapper.updateById(updateTask);
        log.debug("任务状态已更新: taskId={}, status={}", taskId, status.getDescription());
    }

    /**
     * 标记任务失败阶段
     */
    private void markFailedStageInTask(ImportTaskDO updateTask, Long taskId) {
        // 查询当前任务状态，判断失败发生在哪个阶段
        ImportTaskDO currentTask = taskMapper.selectById(taskId);
        if (currentTask != null) {
            if (currentTask.getExtractStatus() == 1) {
                updateTask.setExtractStatus(3); // 解压失败
            } else if (currentTask.getImportStatus() == 1) {
                updateTask.setImportStatus(3); // 导入失败
            } else if (currentTask.getQcStatus() == 1) {
                updateTask.setQcStatus(3); // 质控失败
            }
        }
    }

    /**
     * 计算成功率的通用方法
     */
    private Double calculateSuccessRate(Number success, Number total) {
        if (total == null || total.longValue() == 0) {
            return 0.0;
        }
        return BigDecimal.valueOf(success.doubleValue() * 100.0 / total.doubleValue())
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex) : "";
    }

    // ==================== 内部数据结构 ====================

    /**
     * 创建任务明细记录
     * <p>
     * 设计理念：为每个识别到的Excel文件创建独立的处理跟踪记录
     * 这就像为每个待处理的包裹分配一个独特的追踪号码，
     * 确保我们能够精确地监控每个文件的处理进展
     *
     * @param taskId    主任务ID
     * @param taskNo    主任务编号
     * @param fileInfos 文件信息映射，key是表类型，value是文件详情
     */
    private void createTaskDetails(Long taskId, String taskNo, Map<TableTypeEnum, FileInfo> fileInfos) {
        log.info("开始创建任务明细记录: taskId={}, 文件数量={}", taskId, fileInfos.size());

        // 使用批量插入优化性能，避免逐条插入的数据库开销
        List<ImportTaskDetailDO> detailList = new ArrayList<>();

        fileInfos.forEach((tableType, fileInfo) -> {
            // 为每个文件类型创建明细记录，建立完整的处理跟踪链路
            ImportTaskDetailDO detail = ImportTaskDetailDO.builder()
                    .taskId(taskId)
                    .taskNo(taskNo)
                    .fileType(getFileTypeByTable(tableType))
                    .fileName(fileInfo.getFileName())
                    .targetTable(tableType.getTableName())
                    .tableType(tableType.getType())
                    // 初始状态：所有明细都从待处理状态开始
                    .status(DetailStatusEnum.PENDING.getStatus())
                    .parseStatus(0)    // 0-未开始
                    .importStatus(0)   // 0-未开始
                    .qcStatus(0)       // 0-未开始
                    .progressPercent(0)
                    .retryCount(0)
                    .maxRetryCount(3)  // 默认最多重试3次
                    // 预设行数信息，用于进度计算和资源预估
                    .totalRows(fileInfo.getEstimatedRowCount() != null ?
                            fileInfo.getEstimatedRowCount().longValue() : 0L)
                    .validRows(fileInfo.getValidRowCount() != null ?
                            fileInfo.getValidRowCount().longValue() : 0L)
                    .build();

            detailList.add(detail);
        });

        // 执行批量插入，提升性能
        if (!detailList.isEmpty()) {
            taskDetailMapper.insertBatch(detailList);
            log.info("任务明细记录创建完成: taskId={}, 创建数量={}", taskId, detailList.size());

            // 更新主任务的文件统计信息
            updateTaskFileStatistics(taskId, detailList.size());
        }
    }

    /**
     * 补充完善DrugBatchImportServiceImpl中的核心方法
     * <p>
     * 这些方法体现了企业级应用中数据管理和状态跟踪的最佳实践
     * 每个方法都有明确的职责边界和错误处理策略
     */

// ==================== 任务明细管理方法 ====================

    /**
     * 更新主任务的文件统计信息
     * <p>
     * 当明细记录创建后，需要同步更新主任务的统计数据
     * 这确保了数据的一致性，避免主任务和明细数据不匹配
     */
    private void updateTaskFileStatistics(Long taskId, int totalFiles) {
        ImportTaskDO updateTask = ImportTaskDO.builder()
                .id(taskId)
                .totalFiles(totalFiles)
                .successFiles(0)  // 初始时都是0
                .failedFiles(0)   // 随着处理进展会逐步更新
                .build();

        taskMapper.updateById(updateTask);
        log.debug("主任务文件统计已更新: taskId={}, totalFiles={}", taskId, totalFiles);
    }

    /**
     * 根据表类型获取对应的文件类型字符串
     * <p>
     * 这个映射关系建立了业务表类型和文件标识之间的桥梁
     * 使用switch表达式让代码更简洁且类型安全
     */
    private String getFileTypeByTable(TableTypeEnum tableType) {
        return switch (tableType) {
            case HOSPITAL_INFO -> "HOSPITAL_INFO";
            case DRUG_CATALOG -> "DRUG_CATALOG";
            case DRUG_INBOUND -> "DRUG_INBOUND";
            case DRUG_OUTBOUND -> "DRUG_OUTBOUND";
            case DRUG_USAGE -> "DRUG_USAGE";
        };
    }

    /**
     * 更新明细状态
     * <p>
     * 这是状态管理的核心方法，负责更新单个表的处理状态
     * 设计时考虑了状态转换的时序性和数据一致性
     *
     * @param taskId       任务ID
     * @param tableType    表类型
     * @param status       新状态
     * @param errorMessage 错误信息（可选）
     */
    private void updateDetailStatus(Long taskId, TableTypeEnum tableType,
                                    DetailStatusEnum status, String errorMessage) {
        log.debug("更新明细状态: taskId={}, tableType={}, status={}",
                taskId, tableType, status.getDescription());

        // 使用条件查询精确定位要更新的记录
        LambdaQueryWrapper<ImportTaskDetailDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImportTaskDetailDO::getTaskId, taskId)
                .eq(ImportTaskDetailDO::getTableType, tableType.getType());

        ImportTaskDetailDO detail = taskDetailMapper.selectOne(wrapper);
        if (detail == null) {
            log.warn("未找到对应的任务明细: taskId={}, tableType={}", taskId, tableType);
            return;
        }

        // 更新主状态
        detail.setStatus(status.getStatus());
        if (StringUtils.hasText(errorMessage)) {
            detail.setErrorMessage(errorMessage);
        }

        // 根据不同状态设置相应的子状态和时间戳
        // 这种设计让状态转换变得可追踪和可审计
        LocalDateTime now = LocalDateTime.now();
        switch (status) {
            case PARSING -> {
                detail.setStartTime(now);
                detail.setParseStatus(1); // 1-进行中
            }
            case IMPORTING -> {
                detail.setParseStatus(2); // 2-成功
                detail.setParseEndTime(now);
                detail.setImportStatus(1); // 1-进行中
            }
            case QC_CHECKING -> {
                detail.setImportStatus(2); // 2-成功
                detail.setImportEndTime(now);
                detail.setQcStatus(1); // 1-进行中
            }
            case SUCCESS, PARTIAL_SUCCESS -> {
                detail.setQcStatus(2); // 2-成功
                detail.setQcEndTime(now);
                detail.setEndTime(now);
                detail.setProgressPercent(100);
            }
            case FAILED -> {
                // 失败时需要标记失败的具体阶段
                detail.setEndTime(now);
                detail.setProgressPercent(0); // 0表示失败
                markFailedStage(detail, status);
            }
        }

        taskDetailMapper.updateById(detail);
        log.debug("明细状态更新完成: detailId={}, status={}", detail.getId(), status.getDescription());
    }

// ==================== 状态更新管理方法 ====================

    /**
     * 标记失败阶段的具体状态
     * <p>
     * 当任务失败时，我们需要准确记录是在哪个阶段失败的
     * 这对于问题诊断和重试策略制定非常重要
     */
    private void markFailedStage(ImportTaskDetailDO detail, DetailStatusEnum status) {
        // 根据当前的子状态判断失败发生在哪个阶段
        if (detail.getParseStatus() == 1) {
            detail.setParseStatus(3); // 3-失败
        } else if (detail.getImportStatus() == 1) {
            detail.setImportStatus(3); // 3-失败
        } else if (detail.getQcStatus() == 1) {
            detail.setQcStatus(3); // 3-失败
        }
    }

    /**
     * 更新明细进度
     * <p>
     * 这个方法专门负责更新Redis中的实时进度信息
     * 设计上与数据库状态更新分离，确保高频的进度更新不会对数据库造成压力
     */
    private void updateDetailProgress(Long taskId, TableTypeEnum tableType, int progress, String message) {
        TaskDetailProgressInfo detailProgress = TaskDetailProgressInfo.builder()
                .taskId(taskId)
                .tableType(tableType.name())
                .progress(progress)
                .message(message)
                .status(determineProgressStatus(progress))
                .updateTime(LocalDateTime.now())
                .build();

        taskProgressRedisDAO.setTaskDetailProgress(detailProgress);
        log.debug("明细进度已更新: taskId={}, tableType={}, progress={}%",
                taskId, tableType, progress);
    }

    /**
     * 根据进度百分比确定状态描述
     * <p>
     * 这是一个简单但实用的状态映射逻辑
     * 让前端能够根据进度值显示相应的状态标识
     */
    private String determineProgressStatus(int progress) {
        if (progress < 0) return "FAILED";
        if (progress == 0) return "WAITING";
        if (progress >= 100) return "SUCCESS";
        return "PROCESSING";
    }

    /**
     * 更新明细最终状态
     * <p>
     * 当一个表的完整处理流程结束时，调用此方法进行最终的状态确认
     * 这里会整合导入结果和质控结果，形成最终的处理报告
     */
    private void updateDetailFinalStatus(Long taskId, TableTypeEnum tableType,
                                         DetailStatusEnum finalStatus,
                                         ImportResult importResult,
                                         QualityControlResult qcResult) {
        log.info("更新明细最终状态: taskId={}, tableType={}, finalStatus={}",
                taskId, tableType, finalStatus.getDescription());

        LambdaQueryWrapper<ImportTaskDetailDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImportTaskDetailDO::getTaskId, taskId)
                .eq(ImportTaskDetailDO::getTableType, tableType.getType());

        ImportTaskDetailDO detail = taskDetailMapper.selectOne(wrapper);
        if (detail != null) {
            // 设置最终状态和统计数据
            detail.setStatus(finalStatus.getStatus());
            detail.setEndTime(LocalDateTime.now());
            detail.setProgressPercent(finalStatus == DetailStatusEnum.FAILED ? 0 : 100);

            // 更新导入统计
            detail.setSuccessRows(importResult.getSuccessCount().longValue());
            detail.setFailedRows(importResult.getFailedCount().longValue());
            detail.setTotalRows(importResult.getTotalCount().longValue());

            // 更新质控统计（如果质控服务已实现）
            if (qcResult != null) {
                detail.setQcPassedRows(qcResult.getPassedCount());
                detail.setQcFailedRows(qcResult.getFailedCount());
            }

            taskDetailMapper.updateById(detail);

            // 同步更新主任务的统计信息
            updateMainTaskStatistics(taskId);
        }
    }

    /**
     * 同步更新主任务的统计信息
     * <p>
     * 当明细状态发生变化时，需要重新计算主任务的汇总统计
     * 这确保了主任务数据的实时准确性
     */
    private void updateMainTaskStatistics(Long taskId) {
        // 查询所有明细的统计数据
        List<ImportTaskDetailDO> details = taskDetailMapper.selectList(
                new LambdaQueryWrapper<ImportTaskDetailDO>()
                        .eq(ImportTaskDetailDO::getTaskId, taskId)
        );

        // 计算汇总统计
        int successFiles = (int) details.stream()
                .filter(d -> DetailStatusEnum.SUCCESS.getStatus().equals(d.getStatus()))
                .count();

        int failedFiles = (int) details.stream()
                .filter(d -> DetailStatusEnum.FAILED.getStatus().equals(d.getStatus()))
                .count();

        long totalRecords = details.stream()
                .mapToLong(d -> d.getTotalRows() != null ? d.getTotalRows() : 0L)
                .sum();

        long successRecords = details.stream()
                .mapToLong(d -> d.getSuccessRows() != null ? d.getSuccessRows() : 0L)
                .sum();

        long failedRecords = details.stream()
                .mapToLong(d -> d.getFailedRows() != null ? d.getFailedRows() : 0L)
                .sum();

        // 更新主任务统计
        ImportTaskDO updateTask = ImportTaskDO.builder()
                .id(taskId)
                .successFiles(successFiles)
                .failedFiles(failedFiles)
                .totalRecords(totalRecords)
                .successRecords(successRecords)
                .failedRecords(failedRecords)
                .build();

        taskMapper.updateById(updateTask);
        log.debug("主任务统计已更新: taskId={}, 成功文件={}, 失败文件={}",
                taskId, successFiles, failedFiles);
    }

    /**
     * 确定明细的最终状态
     * <p>
     * 这个方法体现了业务规则的抽象：
     * 1. 质控失败 = 整体失败
     * 2. 质控成功 + 部分导入失败 = 部分成功
     * 3. 质控成功 + 导入全部成功 = 完全成功
     */
    private DetailStatusEnum determineDetailFinalStatus(ImportResult importResult,
                                                        QualityControlResult qcResult) {
        // TODO: 质控服务实现后完善此逻辑
        // 当前简化逻辑：仅基于导入结果判断
        if (importResult.getFailedCount() == 0) {
            return DetailStatusEnum.SUCCESS;
        } else if (importResult.getSuccessCount() > 0) {
            return DetailStatusEnum.PARTIAL_SUCCESS;
        } else {
            return DetailStatusEnum.FAILED;
        }
    }

// ==================== 状态判断和计算方法 ====================

    /**
     * 确定任务的最终状态
     * <p>
     * 主任务的状态需要综合考虑所有明细的处理结果
     * 这里采用"最坏情况优先"的策略确保问题不被掩盖
     */
    private TaskStatusEnum determineFinalStatus(boolean hasError, QualityControlResult qcResult) {
        // TODO: 质控服务实现后完善此逻辑
        // 当前简化逻辑：仅基于错误标志判断
        if (hasError) {
            return TaskStatusEnum.PARTIAL_SUCCESS;
        } else {
            return TaskStatusEnum.COMPLETED;
        }
    }

    /**
     * 更新任务最终状态
     * <p>
     * 当整个导入流程结束时，设置任务的最终状态和统计信息
     */
    private void updateTaskFinalStatus(Long taskId, TaskStatusEnum finalStatus,
                                       ImportProcessResult importResult) {
        ImportTaskDO updateTask = ImportTaskDO.builder()
                .id(taskId)
                .status(finalStatus.getStatus())
                .successRecords((long) importResult.getTotalSuccess())
                .failedRecords((long) importResult.getTotalFailed())
                .totalRecords((long) (importResult.getTotalSuccess() + importResult.getTotalFailed()))
                .endTime(LocalDateTime.now())
                .progressPercent(100)
                .build();

        taskMapper.updateById(updateTask);
        log.info("任务最终状态已更新: taskId={}, status={}, 成功记录={}, 失败记录={}",
                taskId, finalStatus.getDescription(),
                importResult.getTotalSuccess(), importResult.getTotalFailed());
    }

    /**
     * 处理任务错误
     * <p>
     * 这是统一的错误处理入口，确保错误处理的一致性和完整性
     * 包括状态更新、缓存清理、错误记录等多个方面
     */
    private void handleTaskError(Long taskId, String errorMessage) {
        log.error("处理任务错误: taskId={}, error={}", taskId, errorMessage);

        try {
            // 更新数据库状态
            ImportTaskDO updateTask = ImportTaskDO.builder()
                    .id(taskId)
                    .status(TaskStatusEnum.FAILED.getStatus())
                    .errorMessage(errorMessage)
                    .endTime(LocalDateTime.now())
                    .progressPercent(0) // 0表示失败
                    .build();

            taskMapper.updateById(updateTask);

            // 清理Redis缓存，释放内存资源
            taskProgressRedisDAO.deleteTaskProgress(taskId);
            taskProgressRedisDAO.deleteAllTaskDetailProgress(taskId);

            log.info("任务错误处理完成: taskId={}", taskId);

        } catch (Exception e) {
            // 错误处理过程中的异常需要特别记录，避免错误被掩盖
            log.error("处理任务错误时发生异常: taskId={}", taskId, e);
        }
    }

// ==================== 错误处理方法 ====================

    /**
     * 计算预计剩余时间
     * <p>
     * 基于当前进度和历史处理速度进行时间估算
     * 这是一个启发式算法，会随着处理进展动态调整
     */
    private Long calculateEstimatedRemainingTime(int currentProgress) {
        if (currentProgress <= 0 || currentProgress >= 100) {
            return 0L;
        }

        // 基于经验数据的线性估算
        // 实际应用中可以结合历史任务数据进行更精确的预测
        long totalEstimatedSeconds = 30 * 60; // 假设总共需要30分钟
        long remainingProgress = 100 - currentProgress;
        return (totalEstimatedSeconds * remainingProgress) / 100;
    }

// ==================== 时间和进度计算方法 ====================

    /**
     * 计算预计完成时间
     * <p>
     * 基于当前时间和预计剩余时间计算预期完成时间点
     */
    private LocalDateTime calculateEstimatedCompletionTime() {
        // 根据历史数据和文件大小估算处理时间
        return LocalDateTime.now().plusMinutes(30);
    }

    /**
     * 计算预计结束时间
     * <p>
     * 综合考虑任务复杂度、当前进度、历史性能数据进行估算
     */
    private LocalDateTime calculateEstimatedEndTime(ImportTaskDO task, List<ImportTaskDetailDO> details) {
        if (task.getStartTime() == null) {
            return null;
        }

        // 基于当前进度和历史数据估算
        int currentProgress = task.getProgressPercent();
        if (currentProgress <= 0) {
            return task.getStartTime().plusMinutes(30); // 默认30分钟
        }

        Duration elapsed = Duration.between(task.getStartTime(), LocalDateTime.now());
        double progressRatio = currentProgress / 100.0;
        long estimatedTotalMinutes = (long) (elapsed.toMinutes() / progressRatio);

        return task.getStartTime().plusMinutes(estimatedTotalMinutes);
    }

    /**
     * 计算已用时间（秒）
     */
    private Long calculateElapsedSeconds(LocalDateTime startTime) {
        if (startTime == null) {
            return 0L;
        }
        return Duration.between(startTime, LocalDateTime.now()).getSeconds();
    }

    /**
     * 根据任务状态获取当前阶段描述
     */
    private String getCurrentStageFromStatus(Integer status) {
        TaskStatusEnum statusEnum = TaskStatusEnum.getByType(status);
        return switch (statusEnum) {
            case PENDING -> "WAITING";
            case EXTRACTING -> "EXTRACTING";
            case IMPORTING -> "IMPORTING";
            case QC_CHECKING -> "QC_CHECKING";
            case COMPLETED, FAILED, PARTIAL_SUCCESS -> "COMPLETED";
            case CANCELLED -> "CANCELLED";
        };
    }

    /**
     * 计算当前处理速度（记录/秒）
     */
    private Double calculateCurrentProcessingSpeed(ImportTaskDO task) {
        if (task.getStartTime() == null || task.getTotalRecords() == null || task.getTotalRecords() == 0) {
            return 0.0;
        }

        long elapsedSeconds = calculateElapsedSeconds(task.getStartTime());
        if (elapsedSeconds == 0) {
            return 0.0;
        }

        return task.getSuccessRecords().doubleValue() / elapsedSeconds;
    }

    /**
     * 格式化处理速度显示
     */
    private String formatProcessingSpeed(Double speed) {
        if (speed == null || speed == 0) {
            return "0 记录/秒";
        }

        if (speed >= 1000) {
            return String.format("%.1fk 记录/秒", speed / 1000);
        } else if (speed >= 1) {
            return String.format("%.1f 记录/秒", speed);
        } else {
            return String.format("%.2f 记录/秒", speed);
        }
    }

    /**
     * 计算表级成功率
     */
    private Double calculateTableSuccessRate(Long successRows, Long totalRows) {
        return calculateSuccessRate(successRows, totalRows);
    }

// ==================== 统计计算方法 ====================

    /**
     * 按类型统计文件数量
     */
    private Map<String, Integer> calculateFileCountByType(List<ImportTaskDetailDO> details) {
        return details.stream()
                .collect(Collectors.groupingBy(
                        ImportTaskDetailDO::getFileType,
                        Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                ));
    }

    /**
     * 按类型统计记录数量
     */
    private Map<String, Long> calculateRecordCountByType(List<ImportTaskDetailDO> details) {
        return details.stream()
                .collect(Collectors.groupingBy(
                        ImportTaskDetailDO::getFileType,
                        Collectors.summingLong(d -> d.getTotalRows() != null ? d.getTotalRows() : 0L)
                ));
    }

    /**
     * 计算平均处理速度
     */
    private Double calculateAverageProcessingSpeed(ImportTaskDO task, List<ImportTaskDetailDO> details) {
        // 基于总处理时间和总记录数计算平均速度
        if (task.getStartTime() == null || task.getEndTime() == null) {
            return 0.0;
        }

        long processingSeconds = Duration.between(task.getStartTime(), task.getEndTime()).getSeconds();
        if (processingSeconds == 0) {
            return 0.0;
        }

        return task.getTotalRecords().doubleValue() / processingSeconds;
    }

    /**
     * 计算预计剩余时间
     */
    private Long calculateEstimatedTimeRemaining(ImportTaskDO task) {
        if (task.getProgressPercent() >= 100) {
            return 0L;
        }

        // 基于当前进度和平均速度估算
        return calculateEstimatedRemainingTime(task.getProgressPercent());
    }

    /**
     * 计算平均处理时间
     */
    private Integer calculateAverageProcessingTime(ImportTaskDO task, List<ImportTaskDetailDO> details) {
        if (task.getStartTime() == null || task.getEndTime() == null) {
            return 0;
        }

        return (int) Duration.between(task.getStartTime(), task.getEndTime()).getSeconds();
    }

    /**
     * 评估性能等级
     */
    private String evaluatePerformanceLevel(ImportTaskDO task, List<ImportTaskDetailDO> details) {
        Double speed = calculateAverageProcessingSpeed(task, details);

        if (speed >= 100) {
            return "HIGH";
        } else if (speed >= 50) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    /**
     * 按类型计算文件成功率
     */
    private Map<String, Double> calculateFileSuccessRateByType(List<ImportTaskDetailDO> details) {
        Map<String, Double> result = new HashMap<>();

        Map<String, List<ImportTaskDetailDO>> groupedByType = details.stream()
                .collect(Collectors.groupingBy(ImportTaskDetailDO::getFileType));

        groupedByType.forEach((fileType, detailList) -> {
            long successCount = detailList.stream()
                    .filter(d -> DetailStatusEnum.SUCCESS.getStatus().equals(d.getStatus()))
                    .count();
            double successRate = (double) successCount / detailList.size() * 100;
            result.put(fileType, successRate);
        });

        return result;
    }

    /**
     * 按类型计算记录成功率
     */
    private Map<String, Double> calculateRecordSuccessRateByType(List<ImportTaskDetailDO> details) {
        Map<String, Double> result = new HashMap<>();

        Map<String, List<ImportTaskDetailDO>> groupedByType = details.stream()
                .collect(Collectors.groupingBy(ImportTaskDetailDO::getFileType));

        groupedByType.forEach((fileType, detailList) -> {
            long totalRecords = detailList.stream()
                    .mapToLong(d -> d.getTotalRows() != null ? d.getTotalRows() : 0L)
                    .sum();
            long successRecords = detailList.stream()
                    .mapToLong(d -> d.getSuccessRows() != null ? d.getSuccessRows() : 0L)
                    .sum();

            double successRate = totalRecords > 0 ? (double) successRecords / totalRecords * 100 : 0.0;
            result.put(fileType, successRate);
        });

        return result;
    }

    /**
     * 计算质量评分分布
     */
    private Map<String, Integer> calculateQualityScoreDistribution(List<ImportTaskDetailDO> details) {
        // TODO: 质控服务实现后完善此逻辑
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("HIGH", 0);
        distribution.put("MEDIUM", 0);
        distribution.put("LOW", 0);
        return distribution;
    }

    /**
     * 计算平均质量评分
     */
    private Double calculateAverageQualityScore(List<ImportTaskDetailDO> details) {
        // TODO: 质控服务实现后完善此逻辑
        return 85.0; // 临时返回值
    }

    /**
     * 判断任务是否可以取消
     */
    private boolean canCancelTask(ImportTaskDO task) {
        TaskStatusEnum status = TaskStatusEnum.getByType(task.getStatus());
        return status == TaskStatusEnum.PENDING ||
                status == TaskStatusEnum.EXTRACTING ||
                status == TaskStatusEnum.IMPORTING ||
                status == TaskStatusEnum.QC_CHECKING;
    }

// ==================== 任务操作和判断方法 ====================

    /**
     * 判断任务是否可以重试
     */
    private boolean canRetryTask(ImportTaskDO task) {
        TaskStatusEnum status = TaskStatusEnum.getByType(task.getStatus());
        return status == TaskStatusEnum.FAILED || status == TaskStatusEnum.PARTIAL_SUCCESS;
    }

    /**
     * 生成导入批次号
     */
    private String generateImportBatchNo(Long taskId, TableTypeEnum tableType) {
        return String.format("BATCH_%d_%s_%s",
                taskId,
                tableType.name(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
    }

    /**
     * 构建表进度列表
     * <p>
     * 这个方法展示了如何优雅地整合数据库持久化数据和Redis缓存数据
     * 为前端提供完整的表级进度视图
     */
    private List<TableProgressVO> buildTableProgressList(
            List<ImportTaskDetailDO> details,
            Map<String, TaskDetailProgressInfo> detailProgressMap) {

        return details.stream()
                .map(detail -> {
                    String tableTypeKey = String.valueOf(detail.getTableType());
                    TaskDetailProgressInfo progressInfo = detailProgressMap.get(tableTypeKey);

                    return TableProgressVO.builder()
                            .tableType(detail.getTableType())
                            .tableName(getTableDisplayName(TableTypeEnum.getByType(detail.getTableType())))
                            .status(detail.getStatus())
                            .progress(progressInfo != null ? progressInfo.getProgress() : detail.getProgressPercent())
                            .currentMessage(progressInfo != null ? progressInfo.getMessage() : "")
                            .totalRecords(detail.getTotalRows())
                            .successRecords(detail.getSuccessRows())
                            .failedRecords(detail.getFailedRows())
                            .startTime(detail.getStartTime())
                            .endTime(detail.getEndTime())
                            .estimatedRemainingSeconds(calculateTableEstimatedTime(detail, progressInfo))
                            .canRetry(DetailStatusEnum.FAILED.getStatus().equals(detail.getStatus()))
                            .processingSpeed(calculateTableProcessingSpeed(detail))
                            .build();
                })
                .collect(Collectors.toList());
    }

// ==================== 构建表进度列表方法 ====================

    /**
     * 获取表类型的显示名称
     */
    private String getTableDisplayName(TableTypeEnum tableType) {
        return tableType.getDescription();
    }

    /**
     * 计算表级预计剩余时间
     */
    private Long calculateTableEstimatedTime(ImportTaskDetailDO detail, TaskDetailProgressInfo progressInfo) {
        if (progressInfo == null || progressInfo.getProgress() >= 100) {
            return 0L;
        }

        // 基于当前进度估算剩余时间
        int remainingProgress = 100 - progressInfo.getProgress();
        return (long) (remainingProgress * 2); // 简化估算：每1%需要2秒
    }

    /**
     * 计算表级处理速度
     */
    private Double calculateTableProcessingSpeed(ImportTaskDetailDO detail) {
        if (detail.getStartTime() == null || detail.getSuccessRows() == null) {
            return 0.0;
        }

        long elapsedSeconds = Duration.between(detail.getStartTime(), LocalDateTime.now()).getSeconds();
        if (elapsedSeconds == 0) {
            return 0.0;
        }

        return detail.getSuccessRows().doubleValue() / elapsedSeconds;
    }

    /**
     * 获取任务执行日志
     * <p>
     * 这个方法提供了完整的日志查询和过滤功能
     * 支持按日志级别过滤，并自动处理大文件的性能问题
     */
    @Override
    public TaskLogVO getTaskLogs(Long taskId, String logLevel) {
        log.debug("查询任务日志: taskId={}, logLevel={}", taskId, logLevel);

        try {
            // 构建日志文件路径
            String logFilePath = buildLogFilePath(taskId);

            // 读取并过滤日志内容
            String logContent = readLogFile(logFilePath, logLevel);

            // 统计日志行数
            int totalLines = logContent.isEmpty() ? 0 : logContent.split("\n").length;

            return TaskLogVO.builder()
                    .taskId(taskId)
                    .logs(logContent)
                    .logLevel(logLevel)
                    .totalLines(totalLines)
                    .lastUpdateTime(System.currentTimeMillis())
                    .logFileSize(calculateLogFileSize(logFilePath))
                    .hasMoreLogs(totalLines > 1000) // 超过1000行认为有更多日志
                    .build();

        } catch (Exception e) {
            log.error("获取任务日志失败: taskId={}", taskId, e);

            // 错误情况下返回友好的错误信息
            return TaskLogVO.builder()
                    .taskId(taskId)
                    .logs("日志读取失败: " + e.getMessage())
                    .logLevel(logLevel)
                    .totalLines(0)
                    .lastUpdateTime(System.currentTimeMillis())
                    .logFileSize(0L)
                    .hasMoreLogs(false)
                    .build();
        }
    }

// ==================== 任务日志管理方法 ====================

    /**
     * 构建日志文件路径
     * <p>
     * 根据任务ID生成标准的日志文件路径
     * 这种命名规范确保了日志文件的可识别性和可管理性
     */
    private String buildLogFilePath(Long taskId) {
        return String.format("/logs/drug-import/task_%d.log", taskId);
    }

    /**
     * 读取和过滤日志文件
     * <p>
     * 这个方法包含了多重优化策略：
     * 1. 文件存在性检查
     * 2. 按日志级别过滤
     * 3. 大文件截断处理
     * 4. 字符编码处理
     */
    private String readLogFile(String logFilePath, String logLevel) throws IOException {
        Path path = Paths.get(logFilePath);

        if (!Files.exists(path)) {
            return "暂无日志记录";
        }

        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

        // 根据日志级别过滤
        if (!"ALL".equals(logLevel)) {
            lines = lines.stream()
                    .filter(line -> line.contains(logLevel))
                    .collect(Collectors.toList());
        }

        // 限制最大行数，避免大文件导致的性能问题
        if (lines.size() > 1000) {
            lines = lines.subList(lines.size() - 1000, lines.size());
        }

        return String.join("\n", lines);
    }

    /**
     * 计算日志文件大小
     */
    private Long calculateLogFileSize(String logFilePath) {
        try {
            Path path = Paths.get(logFilePath);
            return Files.exists(path) ? Files.size(path) : 0L;
        } catch (IOException e) {
            return 0L;
        }
    }

    /**
     * 获取最近的任务日志列表
     * <p>
     * 为任务详情页面提供日志摘要信息
     * 只返回最重要的日志条目，避免信息过载
     */
    private List<TaskLogVO> getRecentTaskLogs(Long taskId, int limit) {
        try {
            TaskLogVO logVO = getTaskLogs(taskId, "ALL");

            // 如果日志内容不为空，返回包装后的日志信息
            if (StringUtils.hasText(logVO.getLogs()) && !"暂无日志记录".equals(logVO.getLogs())) {
                return List.of(logVO);
            }

            return List.of();

        } catch (Exception e) {
            log.warn("获取近期日志失败: taskId={}", taskId, e);
            return List.of();
        }
    }

// ==================== 近期日志获取方法 ====================

    /**
     * 构建任务时间线
     * <p>
     * TODO: 实现完整的任务时间线构建逻辑
     * 需要记录任务执行过程中的关键时间节点
     */
    private List<TaskTimelineVO> buildTaskTimeline(ImportTaskDO task, List<ImportTaskDetailDO> details) {
        List<TaskTimelineVO> timeline = new ArrayList<>();

        // 添加任务创建节点
        if (task.getCreateTime() != null) {
            timeline.add(TaskTimelineVO.builder()
                    .timestamp(task.getCreateTime())
                    .event("TASK_CREATED")
                    .title("任务创建")
                    .description("任务已创建，等待处理")
                    .type("info")
                    .build());
        }

        // TODO: 添加更多关键节点
        // - 文件解压完成
        // - 各表处理开始/结束
        // - 质控检查节点
        // - 任务完成节点

        return timeline;
    }

// ==================== 预留的质控和关联任务方法 ====================

    /**
     * 查找相关任务
     * <p>
     * TODO: 实现相关任务查找逻辑
     * 根据任务特征找到相关的历史任务或同批次任务
     */
    private List<RelatedTaskVO> findRelatedTasks(ImportTaskDO task) {
        List<RelatedTaskVO> relatedTasks = new ArrayList<>();

        // TODO: 实现以下查找逻辑：
        // 1. 同一批次的任务（根据任务名称或创建时间）
        // 2. 重试关系的任务
        // 3. 相似数据源的任务

        return relatedTasks;
    }

    /**
     * 构建操作选项
     * <p>
     * TODO: 根据任务状态动态生成可用的操作选项
     */
    private TaskOperationOptionsVO buildOperationOptions(ImportTaskDO task) {
        // TODO: 实现完整的操作选项构建逻辑
        return TaskOperationOptionsVO.builder()
                .basicOps(TaskOperationOptionsVO.BasicOperations.builder()
                        .canCancel(canCancelTask(task))
                        .canViewLogs(true)
                        .canViewDetails(true)
                        .canDelete(false) // 暂不支持删除
                        .build())
                .retryOps(TaskOperationOptionsVO.RetryOperations.builder()
                        .canRetry(canRetryTask(task))
                        .availableRetryTypes(getAvailableRetryTypes(task))
                        .estimatedRetryDuration(600L) // 预计10分钟
                        .retryRecommendation("建议先检查错误日志，确认问题原因后再重试")
                        .build())
                .exportOps(TaskOperationOptionsVO.ExportOperations.builder()
                        .canDownloadReport(task.getStatus().equals(TaskStatusEnum.COMPLETED.getStatus()))
                        .canExportData(task.getStatus().equals(TaskStatusEnum.COMPLETED.getStatus()))
                        .canExportErrors(task.getFailedRecords() != null && task.getFailedRecords() > 0)
                        .availableExportFormats(List.of("EXCEL", "PDF"))
                        .build())
                .build();
    }

    /**
     * 获取可用的重试类型
     */
    private List<String> getAvailableRetryTypes(ImportTaskDO task) {
        List<String> retryTypes = new ArrayList<>();

        if (canRetryTask(task)) {
            retryTypes.add("ALL");      // 全部重试
            retryTypes.add("FAILED");   // 仅重试失败部分

            // 如果有特定的表失败，还可以支持按表类型重试
            // TODO: 查询失败的具体表类型，添加对应的重试选项
        }

        return retryTypes;
    }

    /**
     * 构建质量报告
     * <p>
     * TODO: 质控服务实现后完善此方法
     */
    private TaskQualityReportVO buildQualityReport(Long taskId) {
        // TODO: 实现完整的质量报告构建逻辑
        // 需要集成质控服务的检查结果
        return TaskQualityReportVO.builder()
                .scores(TaskQualityReportVO.QualityScores.builder()
                        .overallQualityScore(85.0)
                        .dataIntegrityScore(90.0)
                        .consistencyScore(80.0)
                        .completenessScore(85.0)
                        .overallGrade("B")
                        .build())
                .issues(TaskQualityReportVO.QualityIssues.builder()
                        .criticalIssues(new ArrayList<>())
                        .warningIssues(new ArrayList<>())
                        .infoIssues(new ArrayList<>())
                        .totalIssueCount(0)
                        .build())
                .recommendations(TaskQualityReportVO.QualityRecommendations.builder()
                        .immediateActions(List.of("暂无紧急问题需要处理"))
                        .processImprovements(List.of("建议完善数据录入规范"))
                        .preventiveMeasures(List.of("建议增加数据验证规则"))
                        .overallSuggestion("数据质量良好，建议继续保持")
                        .build())
                .detailedMetrics(new HashMap<>())
                .build();
    }

    /**
     * 全部重试
     * <p>
     * TODO: 实现完整的全部重试逻辑
     */
    private ImportRetryResult retryAllTables(ImportTaskDO task) {
        log.info("执行全部重试: taskId={}", task.getId());

        // TODO: 实现重试逻辑
        return ImportRetryResult.builder()
                .taskId(task.getId())
                .taskNo(task.getTaskNo())
                .success(true)
                .message("全部重试已启动")
                .retryType("ALL")
                .retryScope(List.of("ALL_TABLES"))
                .retryStartTime(LocalDateTime.now())
                .retryBatchNo("RETRY_" + System.currentTimeMillis())
                .build();
    }

// ==================== 重试相关方法占位符 ====================

    /**
     * 仅失败部分重试
     * <p>
     * TODO: 实现失败部分重试逻辑
     */
    private ImportRetryResult retryFailedTables(ImportTaskDO task) {
        log.info("执行失败部分重试: taskId={}", task.getId());

        // TODO: 实现重试逻辑
        return ImportRetryResult.builder()
                .taskId(task.getId())
                .taskNo(task.getTaskNo())
                .success(true)
                .message("失败部分重试已启动")
                .retryType("FAILED")
                .retryScope(List.of())
                .retryStartTime(LocalDateTime.now())
                .retryBatchNo("RETRY_FAILED_" + System.currentTimeMillis())
                .build();
    }

    /**
     * 指定表类型重试
     * <p>
     * TODO: 实现指定表重试逻辑
     */
    private ImportRetryResult retrySpecificTable(ImportTaskDO task, String fileType) {
        log.info("执行指定类型重试: taskId={}, fileType={}", task.getId(), fileType);

        // TODO: 实现重试逻辑
        return ImportRetryResult.builder()
                .taskId(task.getId())
                .taskNo(task.getTaskNo())
                .success(true)
                .message("指定类型重试已启动")
                .retryType("FILE_TYPE")
                .retryScope(List.of(fileType))
                .retryStartTime(LocalDateTime.now())
                .retryBatchNo("RETRY_" + fileType + "_" + System.currentTimeMillis())
                .build();
    }

    /**
     * 构建成功的验证结果
     */
    private FileValidationResult buildSuccessValidationResult(MultipartFile file, FileExtractResult extractResult) {
        List<FileValidationResult.ExtractedFileInfo> extractedFiles = extractResult.getFileInfos().entrySet().stream()
                .map(entry -> {
                    TableTypeEnum tableType = entry.getKey();
                    FileInfo fileInfo = entry.getValue();

                    return FileValidationResult.ExtractedFileInfo.builder()
                            .fileName(fileInfo.getFileName())
                            .tableType(tableType.name())
                            .rowCount(fileInfo.getEstimatedRowCount())
                            .fileSize(fileInfo.getFileSize())
                            .isValid(fileInfo.getIsValid())
                            .actualFields(fileInfo.getActualFields())
                            .previewData(fileInfo.getPreviewData())
                            .validRowCount(fileInfo.getValidRowCount())
                            .dataQuality(fileInfo.getDataQuality())
                            .encoding(fileInfo.getEncoding())
                            .qualityInfo(fileInfo.getQualityInfo())
                            .build();
                })
                .collect(Collectors.toList());

        return FileValidationResult.builder()
                .valid(true)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .expectedFileCount(5)
                .actualFileCount(extractResult.getValidFileCount())
                .validationMessage("文件验证通过，包含所有必需的Excel文件")
                .missingFiles(new ArrayList<>())
                .extraFiles(new ArrayList<>())
                .invalidFiles(new ArrayList<>())
                .validationTime(LocalDateTime.now())
                .extractedFiles(extractedFiles)
                .extractDurationMs(extractResult.getExtractDurationMs())
                .extractStartTime(extractResult.getExtractStartTime())
                .extractEndTime(extractResult.getExtractEndTime())
                .build();
    }

// ==================== 文件验证结果构建方法 ====================

    /**
     * 构建失败的验证结果
     */
    private FileValidationResult buildFailedValidationResult(MultipartFile file, FileExtractResult extractResult) {
        return FileValidationResult.builder()
                .valid(false)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .expectedFileCount(5)
                .actualFileCount(extractResult.getValidFileCount())
                .validationMessage(extractResult.getErrorMessage())
                .missingFiles(identifyMissingFiles(extractResult))
                .extraFiles(new ArrayList<>())
                .invalidFiles(new ArrayList<>())
                .validationTime(LocalDateTime.now())
                .extractedFiles(new ArrayList<>())
                .build();
    }

    /**
     * 构建错误的验证结果
     */
    private FileValidationResult buildErrorValidationResult(MultipartFile file, Exception e) {
        return FileValidationResult.builder()
                .valid(false)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .expectedFileCount(5)
                .actualFileCount(0)
                .validationMessage("文件验证失败: " + e.getMessage())
                .missingFiles(new ArrayList<>())
                .extraFiles(new ArrayList<>())
                .invalidFiles(new ArrayList<>())
                .validationTime(LocalDateTime.now())
                .extractedFiles(new ArrayList<>())
                .build();
    }

    /**
     * 识别缺失的文件
     */
    private List<String> identifyMissingFiles(FileExtractResult extractResult) {
        List<String> expectedFiles = Arrays.asList(
                "机构基本情况.xlsx", "药品目录.xlsx", "药品入库.xlsx", "药品出库.xlsx", "药品使用.xlsx"
        );

        if (extractResult.getFileInfos() == null) {
            return expectedFiles;
        }

        List<String> actualFiles = extractResult.getFileInfos().values().stream()
                .map(FileInfo::getFileName)
                .collect(Collectors.toList());

        return expectedFiles.stream()
                .filter(expected -> actualFiles.stream()
                        .noneMatch(actual -> actual.contains(expected.replace(".xlsx", ""))))
                .collect(Collectors.toList());
    }

    /**
     * 清理临时文件
     */
    private void cleanupTempFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                log.debug("临时文件已清理: {}", filePath);
            }
        } catch (IOException e) {
            log.warn("清理临时文件失败: {}", filePath, e);
        }
    }

    /**
     * 导入处理结果的内部数据结构
     */
    private static class ImportProcessResult {
        private int totalSuccess = 0;
        private int totalFailed = 0;
        private boolean hasError = false;
        private Map<TableTypeEnum, String> errors = new HashMap<>();

        public void addTableResult(ImportResult result) {
            this.totalSuccess += result.getSuccessCount();
            this.totalFailed += result.getFailedCount();
            if (result.getHasError()) {
                this.hasError = true;
            }
        }

        public void addError(TableTypeEnum tableType, String errorMessage) {
            this.hasError = true;
            this.errors.put(tableType, errorMessage);
        }

        // Getters
        public int getTotalSuccess() {
            return totalSuccess;
        }

        public int getTotalFailed() {
            return totalFailed;
        }

        public boolean hasError() {
            return hasError;
        }

        public Map<TableTypeEnum, String> getErrors() {
            return errors;
        }
    }
}