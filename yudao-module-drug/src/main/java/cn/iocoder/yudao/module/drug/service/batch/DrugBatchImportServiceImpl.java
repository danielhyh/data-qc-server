package cn.iocoder.yudao.module.drug.service.batch;

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
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.drug.enums.DrugErrorCodeConstants.*;

/**
 * 药品数据批量导入服务实现 - 修复版
 * <p>
 * 本次修复主要解决了以下问题：
 * 1. 将任务编号生成职责移到Redis DAO层，体现职责分离原则
 * 2. 修复了状态检查的逻辑错误
 * 3. 优化了文件验证逻辑，增强健壮性
 * 4. 完善了异常处理和日志记录
 * <p>
 * 设计理念保持不变：
 * - 统一的任务管理：所有导入操作都通过任务来管理
 * - 分阶段处理：解压 -> 解析 -> 导入 -> 质控
 * - 智能重试：支持精确重试
 * - 实时进度：提供多层次的进度反馈
 *
 * @author yourname
 * @since 2024-05-29
 */
@Service
@Slf4j
@Validated
public class DrugBatchImportServiceImpl implements DrugBatchImportService {

    // 导入顺序（考虑数据依赖关系）
    private static final List<TableTypeEnum> IMPORT_ORDER = List.of(
            TableTypeEnum.HOSPITAL_INFO,    // 机构信息必须最先导入
            TableTypeEnum.DRUG_CATALOG,     // 药品目录其次
            TableTypeEnum.DRUG_INBOUND,     // 入库、出库、使用可以并行
            TableTypeEnum.DRUG_OUTBOUND,
            TableTypeEnum.DRUG_USAGE
    );

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportTaskCreateResult createImportTask(MultipartFile file, String taskName, String description) {
        // 1. 基础验证
        validateImportFile(file);

        // 2. 创建主任务记录
        ImportTaskDO task = createTaskRecord(file, taskName);

        // 3. 异步启动导入流程
        CompletableFuture.runAsync(() -> {
            try {
                executeImportProcess(task, file);
            } catch (Exception e) {
                log.error("导入任务执行失败: taskId={}", task.getId(), e);
                handleTaskError(task.getId(), "导入过程异常: " + e.getMessage());
            }
        }, asyncTaskExecutor);

        // 4. 返回结果
        return ImportTaskCreateResult.builder()
                .taskId(task.getId())
                .taskNo(task.getTaskNo())
                .message("导入任务已创建，正在后台处理")
                .build();
    }

    @Override
    public ImportTaskDetailDO getTaskDetail(Long taskId) {
        return taskDetailService.getImportTaskDetail(taskId);
    }

    /**
     * 执行完整的导入流程
     * 体现了整个导入的核心逻辑和状态流转
     */
    private void executeImportProcess(ImportTaskDO task, MultipartFile file) {
        Long taskId = task.getId();

        try {
            // 第一阶段：文件解压和验证
            log.info("开始执行导入任务: taskId={}, taskNo={}", taskId, task.getTaskNo());
            updateTaskStatus(taskId, TaskStatusEnum.EXTRACTING);

            // 更新进度
            updateTaskProgress(taskId, 10, "正在解压文件...", "EXTRACTING");

            FileExtractResult extractResult = fileExtractService.extractAndValidate(taskId, file);
            if (!extractResult.getSuccess()) {
                throw exception(ZIP_EXTRACT_FAILED, extractResult.getErrorMessage());
            }

            // 创建任务明细记录
            createTaskDetails(taskId, task.getTaskNo(), extractResult.getFileInfos());
            updateTaskProgress(taskId, 20, "文件解压完成，准备导入数据...", "EXTRACTING");

            // 第二阶段：按顺序解析和导入数据
            updateTaskStatus(taskId, TaskStatusEnum.IMPORTING);

            boolean hasError = false;
            int totalSuccess = 0;
            int totalFailed = 0;
            int processedTables = 0;

            // 按预定义顺序处理每个文件
            for (TableTypeEnum tableType : IMPORT_ORDER) {
                FileInfo fileInfo = extractResult.getFileInfo(tableType);
                if (fileInfo == null) {
                    log.warn("未找到对应文件: tableType={}", tableType);
                    continue;
                }

                try {
                    // 计算当前进度
                    int currentProgress = 20 + (processedTables * 50 / IMPORT_ORDER.size());
                    updateTaskProgress(taskId, currentProgress,
                            String.format("正在处理%s...", getTableDisplayName(tableType)), "IMPORTING");

                    ImportResult result = processTableData(taskId, tableType, fileInfo);
                    totalSuccess += result.getSuccessCount();
                    totalFailed += result.getFailedCount();

                    if (result.getHasError()) {
                        hasError = true;
                    }

                    processedTables++;

                } catch (Exception e) {
                    log.error("处理表数据失败: taskId={}, tableType={}", taskId, tableType, e);
                    hasError = true;
                    updateDetailStatus(taskId, tableType, DetailStatusEnum.FAILED, e.getMessage());
                }
            }

            // 第三阶段：执行质控检查
            updateTaskStatus(taskId, TaskStatusEnum.QC_CHECKING);
            updateTaskProgress(taskId, 80, "正在执行质控检查...", "QC_CHECKING");

            // 执行质控检查
            QualityControlResult qcResult = qualityControlService.executeOverallQualityControl(taskId);

            // 第四阶段：更新最终状态
            TaskStatusEnum finalStatus = determineFinalStatus(hasError, qcResult);
            updateTaskFinalStatus(taskId, finalStatus, totalSuccess, totalFailed);
            updateTaskProgress(taskId, 100, "任务处理完成", finalStatus.name());

            log.info("导入任务完成: taskId={}, status={}, success={}, failed={}",
                    taskId, finalStatus, totalSuccess, totalFailed);

        } catch (Exception e) {
            log.error("导入任务执行异常: taskId={}", taskId, e);
            handleTaskError(taskId, e.getMessage());
            updateTaskProgress(taskId, -1, "任务执行失败: " + e.getMessage(), "FAILED");
        }
    }

    /**
     * 更新任务进度
     *
     * @param taskId       任务ID
     * @param progress     进度百分比
     * @param message      状态描述
     * @param currentStage 当前阶段
     */
    private void updateTaskProgress(Long taskId, int progress, String message, String currentStage) {
        // 构建进度信息对象
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
     * 更新任务明细进度
     */
    private void updateDetailProgress(Long taskId, TableTypeEnum tableType, int progress, String message) {
        TaskDetailProgressInfo detailProgress = TaskDetailProgressInfo.builder()
                .taskId(taskId)
                .tableType(tableType.name())
                .progress(progress)
                .message(message)
                .status("PROCESSING")
                .updateTime(LocalDateTime.now())
                .build();

        taskProgressRedisDAO.setTaskDetailProgress(detailProgress);
    }

    /**
     * 处理单个表的数据导入
     */
    private ImportResult processTableData(Long taskId, TableTypeEnum tableType, FileInfo fileInfo) {
        log.info("开始处理表数据: taskId={}, tableType={}, fileName={}",
                taskId, tableType, fileInfo.getFileName());

        // 1. 更新明细状态为解析中
        updateDetailStatus(taskId, tableType, DetailStatusEnum.PARSING, null);
        updateDetailProgress(taskId, tableType, 10, "正在解析Excel文件...");

        // 2. 解析Excel文件
        ParseResult parseResult = dataParseService.parseExcelFile(fileInfo, tableType);
        if (!parseResult.getSuccess()) {
            throw exception(FILE_READ_ERROR, parseResult.getErrorMessage());
        }

        updateDetailProgress(taskId, tableType, 30,
                String.format("解析完成，共%d条数据", parseResult.getTotalRows()));

        // 3. 更新明细状态为导入中
        updateDetailStatus(taskId, tableType, DetailStatusEnum.IMPORTING, null);

        // 4. 分批执行数据导入（避免大事务）
        ImportResult importResult = batchImportData(taskId, tableType, parseResult.getDataList());

        updateDetailProgress(taskId, tableType, 70,
                String.format("导入完成，成功%d条，失败%d条",
                        importResult.getSuccessCount(), importResult.getFailedCount()));

        // 5. 更新明细状态为质控中
        updateDetailStatus(taskId, tableType, DetailStatusEnum.QC_CHECKING, null);

        // 6. 执行表级质控
        QualityControlResult qcResult = qualityControlService.executeTableQualityControl(taskId, tableType);

        updateDetailProgress(taskId, tableType, 100,
                String.format("质控完成，通过%d条，失败%d条",
                        qcResult.getPassedCount(), qcResult.getFailedCount()));

        // 7. 确定最终状态
        DetailStatusEnum finalStatus = qcResult.getSuccess() ?
                (importResult.getHasError() ? DetailStatusEnum.PARTIAL_SUCCESS : DetailStatusEnum.SUCCESS) :
                DetailStatusEnum.FAILED;

        updateDetailFinalStatus(taskId, tableType, finalStatus, importResult, qcResult);

        return importResult;
    }

    /**
     * 分批导入数据
     */
    private ImportResult batchImportData(Long taskId, TableTypeEnum tableType, List<?> dataList) {
        int batchSize = 1000; // 每批处理1000条
        int totalRows = dataList.size();
        int successCount = 0;
        int failedCount = 0;

        for (int i = 0; i < totalRows; i += batchSize) {
            int end = Math.min(i + batchSize, totalRows);
            List<?> batch = dataList.subList(i, end);

            try {
                // 执行批量插入
                ImportResult batchResult = dataImportService.importBatch(taskId, tableType, batch);
                successCount += batchResult.getSuccessCount();
                failedCount += batchResult.getFailedCount();

                // 更新明细进度
                int progress = 30 + (end * 40 / totalRows);
                updateDetailProgress(taskId, tableType, progress,
                        String.format("正在导入数据...(%d/%d)", end, totalRows));

            } catch (Exception e) {
                log.error("批量导入失败: taskId={}, tableType={}, batch={}-{}",
                        taskId, tableType, i, end, e);
                failedCount += batch.size();
            }
        }

        return ImportResult.builder()
                .successCount(successCount)
                .failedCount(failedCount)
                .totalCount(successCount + failedCount)
                .hasError(failedCount > 0)
                .build();
    }

    @Override
    public ImportProgressVO getTaskProgress(Long taskId) {
        // 查询主任务信息
        ImportTaskDO task = taskMapper.selectById(taskId);
        if (task == null) {
            throw exception(TASK_NOT_FOUND);
        }

        // 查询任务明细
        List<ImportTaskDetailDO> details = taskDetailMapper.selectList(new LambdaQueryWrapper<ImportTaskDetailDO>()
                .eq(ImportTaskDetailDO::getTaskId, taskId));

        TaskProgressInfo progressInfo = taskProgressRedisDAO.getTaskProgress(taskId);

        // 获取所有明细进度信息
        Map<String, TaskDetailProgressInfo> detailProgressMap =
                taskProgressRedisDAO.getAllTaskDetailProgress(taskId);

        // 构建进度信息
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
                .totalFiles(task.getTotalFiles())
                .successFiles(task.getSuccessFiles())
                .failedFiles(task.getFailedFiles())
                .totalRecords(task.getTotalRecords())
                .successRecords(task.getSuccessRecords())
                .failedRecords(task.getFailedRecords())
                .tableProgress(buildTableProgressList(details, detailProgressMap))
                .canRetry(canRetryTask(task))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportRetryResult retryImport(Long taskId, RetryTypeEnum retryType, String fileType) {
        // 验证任务状态
        ImportTaskDO task = taskMapper.selectById(taskId);
        if (task == null) {
            throw exception(TASK_NOT_FOUND);
        }

        if (!canRetryTask(task)) {
            throw exception(IMPORT_RETRY_NOT_SUPPORTED);
        }

        // 使用分布式锁确保重试操作的安全性
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (!taskProgressRedisDAO.tryLockTask(taskId, userId)) {
            throw exception(IMPORT_TASK_LOCKED);
        }

        try {
            log.info("开始重试导入任务: taskId={}, retryType={}, fileType={}, userId={}",
                    taskId, retryType, fileType, userId);

            // 根据重试类型执行不同的重试策略
            return switch (retryType) {
                case ALL -> retryAllTables(task);
                case FAILED -> retryFailedTables(task);
                case FILE_TYPE -> retrySpecificTable(task, fileType);
                default -> throw exception(IMPORT_RETRY_TYPE_UNSUPPORTED);
            };
        } finally {
            // 确保锁被释放
            taskProgressRedisDAO.unlockTask(taskId, userId);
        }
    }

    @Override
    public void cancelTask(Long taskId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();

        // 尝试获取任务锁
        if (!taskProgressRedisDAO.tryLockTask(taskId, userId)) {
            throw exception(IMPORT_TASK_LOCKED);
        }

        try {
            // 执行取消逻辑
            log.info("取消导入任务: taskId={}, userId={}", taskId, userId);

            // 更新数据库状态
            updateTaskStatus(taskId, TaskStatusEnum.CANCELLED);

            // 清理Redis缓存
            taskProgressRedisDAO.deleteTaskProgress(taskId);
            taskProgressRedisDAO.deleteAllTaskDetailProgress(taskId);

        } finally {
            taskProgressRedisDAO.unlockTask(taskId, userId);
        }
    }

    @Override
    public PageResult<ImportTaskDO> getTaskPage(ImportTaskPageReqVO pageReqVO) {
        return taskMapper.selectPage(pageReqVO);
    }

    /**
     * 验证导入文件
     * <p>
     * 在文件上传前进行预验证，提供快速反馈
     */
    @Override
    public FileValidationResult validateImportFile(MultipartFile file) {
        log.info("开始验证导入文件: fileName={}, fileSize={}",
                file.getOriginalFilename(), file.getSize());

        try {
            // 基础文件验证
            validateBasicFileProperties(file);

            // 创建临时验证任务ID
            Long tempTaskId = System.currentTimeMillis();

            // 执行文件解压和验证
            FileExtractResult extractResult = fileExtractService.extractAndValidate(tempTaskId, file);

            if (extractResult.getSuccess()) {
                return FileValidationResult.builder()
                        .valid(true)
                        .fileName(file.getOriginalFilename())
                        .fileSize(file.getSize())
                        .expectedFileCount(5) // 预期5个Excel文件
                        .actualFileCount(extractResult.getValidFileCount())
                        .validationMessage("文件验证通过，包含所有必需的Excel文件")
                        .validationTime(LocalDateTime.now())
                        .build();
            } else {
                return FileValidationResult.builder()
                        .valid(false)
                        .fileName(file.getOriginalFilename())
                        .fileSize(file.getSize())
                        .expectedFileCount(5)
                        .actualFileCount(extractResult.getValidFileCount())
                        .validationMessage(extractResult.getErrorMessage())
                        .missingFiles(identifyMissingFiles(extractResult))
                        .validationTime(LocalDateTime.now())
                        .build();
            }

        } catch (Exception e) {
            log.error("文件验证失败: fileName={}", file.getOriginalFilename(), e);

            return FileValidationResult.builder()
                    .valid(false)
                    .fileName(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .validationMessage("文件验证失败: " + e.getMessage())
                    .validationTime(LocalDateTime.now())
                    .build();
        }
    }

    /**
     * 导出任务列表
     * <p>
     * 将任务数据导出为Excel文件
     */
    public void exportTaskList(ImportTaskPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        log.info("开始导出任务列表: params={}", pageReqVO);

        // 设置不分页，获取所有符合条件的数据
        pageReqVO.setPageSize(Integer.MAX_VALUE);
        PageResult<ImportTaskDO> pageResult = taskMapper.selectPage(pageReqVO);

        List<ImportTaskDO> taskList = pageResult.getList();

        // 设置响应头
        String fileName = "药品导入任务列表_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

        // 使用EasyExcel导出
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            EasyExcel.write(outputStream, ImportTaskExportDTO.class)
                    .sheet("任务列表")
                    .doWrite(convertToExportDTO(taskList));
        }

        log.info("任务列表导出完成: count={}", taskList.size());
    }

    /**
     * 获取任务执行日志
     * <p>
     * 提供任务执行过程的详细日志信息
     */
    public TaskLogVO getTaskLogs(Long taskId, String logLevel) {
        log.debug("查询任务日志: taskId={}, logLevel={}", taskId, logLevel);

        try {
            // 构建日志文件路径
            String logFilePath = buildLogFilePath(taskId);

            // 读取日志文件
            String logContent = readLogFile(logFilePath, logLevel);

            // 统计日志行数
            int totalLines = logContent.split("\n").length;

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

    // ==================== 私有辅助方法 ====================

    private void validateBasicFileProperties(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        String fileName = file.getOriginalFilename();
        if (!StringUtils.hasText(fileName)) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String extension = getFileExtension(fileName).toLowerCase();
        if (!".zip".equals(extension) && !".rar".equals(extension)) {
            throw new IllegalArgumentException("仅支持ZIP和RAR格式的压缩文件");
        }

        long maxSize = 100 * 1024 * 1024L; // 100MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小不能超过100MB");
        }
    }

    private List<String> identifyMissingFiles(FileExtractResult extractResult) {
        List<String> expectedFiles = Arrays.asList(
                "机构基本情况.xlsx", "药品目录.xlsx", "药品入库.xlsx", "药品出库.xlsx", "药品使用.xlsx"
        );

        List<String> actualFiles = extractResult.getFileInfos().values().stream()
                .map(FileInfo::getFileName)
                .collect(Collectors.toList());

        return expectedFiles.stream()
                .filter(expected -> actualFiles.stream()
                        .noneMatch(actual -> actual.contains(expected.replace(".xlsx", ""))))
                .collect(Collectors.toList());
    }

    private List<ImportTaskExportDTO> convertToExportDTO(List<ImportTaskDO> taskList) {
        return taskList.stream()
                .map(this::convertToExportDTO)
                .collect(Collectors.toList());
    }

    private ImportTaskExportDTO convertToExportDTO(ImportTaskDO task) {
        return ImportTaskExportDTO.builder()
                .taskNo(task.getTaskNo())
                .taskName(task.getTaskName())
                .fileName(task.getFileName())
                .statusDisplay(getStatusDisplayText(task.getStatus()))
                .totalFiles(task.getTotalFiles())
                .successFiles(task.getSuccessFiles())
                .failedFiles(task.getFailedFiles())
                .totalRecords(task.getTotalRecords())
                .successRecords(task.getSuccessRecords())
                .failedRecords(task.getFailedRecords())
                .progressPercent(task.getProgressPercent())
                .startTime(task.getStartTime())
                .endTime(task.getEndTime())
                .createTime(task.getCreateTime())
                .creator(task.getCreator())
                .build();
    }

    private String getStatusDisplayText(Integer status) {
        return TaskStatusEnum.valueOf(String.valueOf(status)).getDescription();
    }

    private String buildLogFilePath(Long taskId) {
        return String.format("/logs/drug-import/task_%d.log", taskId);
    }

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

        // 限制最大行数
        if (lines.size() > 1000) {
            lines = lines.subList(lines.size() - 1000, lines.size());
        }

        return String.join("\n", lines);
    }

    private Long calculateLogFileSize(String logFilePath) {
        try {
            Path path = Paths.get(logFilePath);
            return Files.exists(path) ? Files.size(path) : 0L;
        } catch (IOException e) {
            return 0L;
        }
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex) : "";
    }

    /**
     * 创建任务记录 - 修复版
     * <p>
     * 修复说明：
     * 1. 将任务编号生成职责移到TaskProgressRedisDAO中
     * 2. 增强了异常处理机制
     * 3. 优化了日志记录
     */
    private ImportTaskDO createTaskRecord(MultipartFile file, String taskName) {
        // 使用Redis DAO生成任务编号，体现职责分离
        String taskNo = taskProgressRedisDAO.generateTaskNo();

        // 保存文件到指定路径
        String filePath = saveUploadedFile(file, taskNo);

        ImportTaskDO task = ImportTaskDO.builder()
                .taskNo(taskNo)
                .taskName(taskName)
                .importType(2) // 压缩包导入
                .fileName(file.getOriginalFilename())
                .filePath(filePath)
                .fileSize(file.getSize())
                .totalFiles(0) // 初始为0，解压后更新
                .successFiles(0)
                .failedFiles(0)
                .totalRecords(0L)
                .successRecords(0L)
                .failedRecords(0L)
                .status(TaskStatusEnum.PENDING.getStatus())
                .extractStatus(0) // 未开始
                .importStatus(0) // 未开始
                .qcStatus(0) // 未开始
                .progressPercent(0)
                .build();

        taskMapper.insert(task);
        log.info("任务记录创建成功: taskId={}, taskNo={}", task.getId(), taskNo);
        return task;
    }

    /**
     * 保存上传文件
     */
    private String saveUploadedFile(MultipartFile file, String taskNo) {
        try {
            String uploadDir = "/data/drug-import/uploads/";
            String fileName = taskNo + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);

            // 确保目录存在
            Files.createDirectories(filePath.getParent());

            // 保存文件
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return filePath.toString();
        } catch (IOException e) {
            throw exception(FILE_UPLOAD_FAILED, "文件保存失败: " + e.getMessage());
        }
    }

    /**
     * 处理任务错误
     */
    private void handleTaskError(Long taskId, String errorMessage) {
        try {
            ImportTaskDO updateTask = ImportTaskDO.builder()
                    .id(taskId)
                    .status(TaskStatusEnum.FAILED.getStatus())
                    .errorMessage(errorMessage)
                    .endTime(LocalDateTime.now())
                    .progressPercent(-1) // -1表示失败
                    .build();

            taskMapper.updateById(updateTask);

            // 清理Redis缓存
            taskProgressRedisDAO.deleteTaskProgress(taskId);
            taskProgressRedisDAO.deleteAllTaskDetailProgress(taskId);

            log.error("任务处理失败: taskId={}, error={}", taskId, errorMessage);
        } catch (Exception e) {
            log.error("处理任务错误时异常: taskId={}", taskId, e);
        }
    }

    /**
     * 更新任务状态 - 修复版
     * <p>
     * 修复说明：
     * 1. 修正了状态判断逻辑，避免不可能的条件分支
     * 2. 优化了时间字段的设置逻辑
     * 3. 增强了代码的可读性和维护性
     */
    private void updateTaskStatus(Long taskId, TaskStatusEnum status) {
        ImportTaskDO updateTask = ImportTaskDO.builder()
                .id(taskId)
                .status(status.getStatus())
                .build();

        // 根据状态设置对应的时间字段
        LocalDateTime now = LocalDateTime.now();
        switch (status) {
            case EXTRACTING:
                updateTask.setStartTime(now);
                break;
            case IMPORTING:
                updateTask.setExtractEndTime(now);
                break;
            case QC_CHECKING:
                updateTask.setImportEndTime(now);
                break;
            case COMPLETED:
            case FAILED:
            case PARTIAL_SUCCESS:
                // 修复：移除了不可能的条件判断
                // 原代码在PARTIAL_SUCCESS分支中检查status == QC_CHECKING，这永远不会为真
                updateTask.setEndTime(now);
                break;
            case CANCELLED:
                updateTask.setEndTime(now);
                break;
        }

        // 如果是从QC_CHECKING状态转换到最终状态，需要设置QC结束时间
        if (status == TaskStatusEnum.COMPLETED || status == TaskStatusEnum.FAILED ||
                status == TaskStatusEnum.PARTIAL_SUCCESS) {
            updateTask.setQcEndTime(now);
        }

        taskMapper.updateById(updateTask);
        log.debug("任务状态已更新: taskId={}, status={}", taskId, status.getDescription());
    }

    /**
     * 创建任务明细记录 - 修复版
     * <p>
     * 修复说明：根据实际解压的文件信息创建明细记录，而不是预定义的所有类型
     */
    private void createTaskDetails(Long taskId, String taskNo, Map<TableTypeEnum, FileInfo> fileInfos) {
        // 为实际存在的文件类型创建明细记录
        fileInfos.forEach((tableType, fileInfo) -> {
            ImportTaskDetailDO detail = ImportTaskDetailDO.builder()
                    .taskId(taskId)
                    .taskNo(taskNo)
                    .fileType(getFileTypeByTable(tableType))
                    .fileName(fileInfo.getFileName())
                    .targetTable(tableType.getTableName())
                    .tableType(tableType.getType())
                    .status(DetailStatusEnum.PENDING.getStatus())
                    .parseStatus(0) // 未开始
                    .importStatus(0) // 未开始
                    .qcStatus(0) // 未开始
                    .progressPercent(0)
                    .retryCount(0)
                    .maxRetryCount(3)
                    .totalRows((long) fileInfo.getEstimatedRowCount())
                    .build();

            taskDetailMapper.insert(detail);
        });

        log.info("任务明细记录创建完成: taskId={}, count={}", taskId, fileInfos.size());
    }

    /**
     * 获取表类型对应的文件类型
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
     * 获取表类型的显示名称
     */
    private String getTableDisplayName(TableTypeEnum tableType) {
        return tableType.getDescription();
    }

    /**
     * 确定最终状态
     * 基于导入结果和质控结果综合判断任务最终状态
     */
    private TaskStatusEnum determineFinalStatus(boolean hasError, QualityControlResult qcResult) {
        if (!qcResult.getSuccess()) {
            return TaskStatusEnum.FAILED;
        }

        if (hasError) {
            return TaskStatusEnum.PARTIAL_SUCCESS;
        }

        return TaskStatusEnum.COMPLETED;
    }

    /**
     * 更新任务最终状态
     */
    private void updateTaskFinalStatus(Long taskId, TaskStatusEnum finalStatus,
                                       int totalSuccess, int totalFailed) {
        ImportTaskDO updateTask = ImportTaskDO.builder()
                .id(taskId)
                .status(finalStatus.getStatus())
                .successRecords((long) totalSuccess)
                .failedRecords((long) totalFailed)
                .totalRecords((long) (totalSuccess + totalFailed))
                .endTime(LocalDateTime.now())
                .progressPercent(100)
                .build();

        taskMapper.updateById(updateTask);
        log.info("任务最终状态已更新: taskId={}, status={}, success={}, failed={}",
                taskId, finalStatus.getDescription(), totalSuccess, totalFailed);
    }

    /**
     * 更新明细状态
     */
    private void updateDetailStatus(Long taskId, TableTypeEnum tableType,
                                    DetailStatusEnum status, String errorMessage) {
        LambdaQueryWrapper<ImportTaskDetailDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImportTaskDetailDO::getTaskId, taskId)
                .eq(ImportTaskDetailDO::getTableType, tableType.getType());

        ImportTaskDetailDO detail = taskDetailMapper.selectOne(wrapper);
        if (detail != null) {
            detail.setStatus(status.getStatus());
            if (errorMessage != null) {
                detail.setErrorMessage(errorMessage);
            }

            // 根据状态设置对应的时间和状态字段
            LocalDateTime now = LocalDateTime.now();
            switch (status) {
                case PARSING:
                    detail.setStartTime(now);
                    detail.setParseStatus(1); // 进行中
                    break;
                case IMPORTING:
                    detail.setParseStatus(2); // 成功
                    detail.setParseEndTime(now);
                    detail.setImportStatus(1); // 进行中
                    break;
                case QC_CHECKING:
                    detail.setImportStatus(2); // 成功
                    detail.setImportEndTime(now);
                    detail.setQcStatus(1); // 进行中
                    break;
                case SUCCESS:
                case PARTIAL_SUCCESS:
                    detail.setQcStatus(2); // 成功
                    detail.setQcEndTime(now);
                    detail.setEndTime(now);
                    detail.setProgressPercent(100);
                    break;
                case FAILED:
                    detail.setEndTime(now);
                    detail.setProgressPercent(-1);
                    break;
            }

            taskDetailMapper.updateById(detail);
        }
    }

    /**
     * 更新明细最终状态
     */
    private void updateDetailFinalStatus(Long taskId, TableTypeEnum tableType,
                                         DetailStatusEnum finalStatus,
                                         ImportResult importResult,
                                         QualityControlResult qcResult) {
        LambdaQueryWrapper<ImportTaskDetailDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImportTaskDetailDO::getTaskId, taskId)
                .eq(ImportTaskDetailDO::getTableType, tableType.getType());

        ImportTaskDetailDO detail = taskDetailMapper.selectOne(wrapper);
        if (detail != null) {
            detail.setStatus(finalStatus.getStatus());
            detail.setEndTime(LocalDateTime.now());
            detail.setSuccessRows((long) importResult.getSuccessCount());
            detail.setFailedRows((long) importResult.getFailedCount());
            detail.setTotalRows((long) importResult.getTotalCount());
            detail.setQcPassedRows(qcResult.getPassedCount());
            detail.setQcFailedRows(qcResult.getFailedCount());
            detail.setProgressPercent(100);

            taskDetailMapper.updateById(detail);
        }
    }

    /**
     * 计算预计结束时间
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
     * 判断任务是否可以重试
     */
    private boolean canRetryTask(ImportTaskDO task) {
        TaskStatusEnum status = TaskStatusEnum.valueOf(String.valueOf(task.getStatus()));
        return status == TaskStatusEnum.FAILED || status == TaskStatusEnum.PARTIAL_SUCCESS;
    }

    /**
     * 全部重试
     */
    private ImportRetryResult retryAllTables(ImportTaskDO task) {
        log.info("执行全部重试: taskId={}", task.getId());

        // 重置任务状态
        resetTaskStatus(task.getId());

        // 生成重试批次号
        String retryBatchNo = taskProgressRedisDAO.generateRetryBatchNo(task.getId(), "ALL");

        // 重新启动导入流程
        CompletableFuture.runAsync(() -> {
            try {
                // 重新读取文件执行导入
                MultipartFile file = reconstructFileFromPath(task.getFilePath());
                executeImportProcess(task, file);
            } catch (Exception e) {
                log.error("全部重试失败: taskId={}", task.getId(), e);
                handleTaskError(task.getId(), "重试失败: " + e.getMessage());
            }
        }, asyncTaskExecutor);

        return ImportRetryResult.builder()
                .taskId(task.getId())
                .taskNo(task.getTaskNo())
                .success(true)
                .message("全部重试已启动")
                .retryType("ALL")
                .retryScope(List.of("ALL_TABLES"))
                .retryStartTime(LocalDateTime.now())
                .retryBatchNo(retryBatchNo)
                .build();
    }

    /**
     * 仅失败部分重试
     */
    private ImportRetryResult retryFailedTables(ImportTaskDO task) {
        log.info("执行失败部分重试: taskId={}", task.getId());

        // 查询失败的明细
        List<ImportTaskDetailDO> failedDetails = getFailedTaskDetails(task.getId());

        if (failedDetails.isEmpty()) {
            return ImportRetryResult.builder()
                    .taskId(task.getId())
                    .taskNo(task.getTaskNo())
                    .success(false)
                    .message("没有找到失败的处理项")
                    .build();
        }

        List<String> retryScope = failedDetails.stream()
                .map(detail -> TableTypeEnum.getByType(detail.getTableType()).name())
                .collect(Collectors.toList());

        // 生成重试批次号
        String retryBatchNo = taskProgressRedisDAO.generateRetryBatchNo(task.getId(), "FAILED");

        // 重试失败的表
        retrySpecificTables(task, failedDetails);

        return ImportRetryResult.builder()
                .taskId(task.getId())
                .taskNo(task.getTaskNo())
                .success(true)
                .message("失败部分重试已启动")
                .retryType("FAILED")
                .retryScope(retryScope)
                .retryStartTime(LocalDateTime.now())
                .retryBatchNo(retryBatchNo)
                .build();
    }

    /**
     * 指定表类型重试
     */
    private ImportRetryResult retrySpecificTable(ImportTaskDO task, String fileType) {
        log.info("执行指定类型重试: taskId={}, fileType={}", task.getId(), fileType);

        TableTypeEnum tableType;
        try {
            tableType = TableTypeEnum.valueOf(fileType);
        } catch (IllegalArgumentException e) {
            return ImportRetryResult.builder()
                    .taskId(task.getId())
                    .taskNo(task.getTaskNo())
                    .success(false)
                    .message("不支持的文件类型: " + fileType)
                    .build();
        }

        // 生成重试批次号
        String retryBatchNo = taskProgressRedisDAO.generateRetryBatchNo(task.getId(), fileType);

        // 重试指定表
        retrySpecificTables(task, Collections.singletonList(getTaskDetailByTableType(task.getId(), tableType)));

        return ImportRetryResult.builder()
                .taskId(task.getId())
                .taskNo(task.getTaskNo())
                .success(true)
                .message("指定类型重试已启动")
                .retryType("FILE_TYPE")
                .retryScope(Collections.singletonList(fileType))
                .retryStartTime(LocalDateTime.now())
                .retryBatchNo(retryBatchNo)
                .build();
    }

    /**
     * 重置任务状态
     */
    private void resetTaskStatus(Long taskId) {
        ImportTaskDO updateTask = ImportTaskDO.builder()
                .id(taskId)
                .status(TaskStatusEnum.PENDING.getStatus())
                .extractStatus(0)
                .importStatus(0)
                .qcStatus(0)
                .progressPercent(0)
                .errorMessage(null)
                .errorDetail(null)
                .startTime(null)
                .endTime(null)
                .build();

        taskMapper.updateById(updateTask);
    }

    /**
     * 获取失败的任务明细
     */
    private List<ImportTaskDetailDO> getFailedTaskDetails(Long taskId) {
        LambdaQueryWrapper<ImportTaskDetailDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImportTaskDetailDO::getTaskId, taskId)
                .eq(ImportTaskDetailDO::getStatus, DetailStatusEnum.FAILED.getStatus());

        return taskDetailMapper.selectList(wrapper);
    }

    /**
     * 重试指定的表
     */
    private void retrySpecificTables(ImportTaskDO task, List<ImportTaskDetailDO> detailsToRetry) {
        CompletableFuture.runAsync(() -> {
            try {
                for (ImportTaskDetailDO detail : detailsToRetry) {
                    TableTypeEnum tableType = TableTypeEnum.getByType(detail.getTableType());

                    // 重置明细状态
                    resetDetailStatus(detail.getId());

                    // 重新处理该表
                    log.info("重试处理表: taskId={}, tableType={}", task.getId(), tableType);

                    // 实际的重试逻辑...
                }
            } catch (Exception e) {
                log.error("指定表重试失败: taskId={}", task.getId(), e);
            }
        }, asyncTaskExecutor);
    }

    /**
     * 重置明细状态
     */
    private void resetDetailStatus(Long detailId) {
        ImportTaskDetailDO updateDetail = ImportTaskDetailDO.builder()
                .id(detailId)
                .status(DetailStatusEnum.PENDING.getStatus())
                .parseStatus(0)
                .importStatus(0)
                .qcStatus(0)
                .progressPercent(0)
                .errorMessage(null)
                .startTime(null)
                .endTime(null)
                .build();

        taskDetailMapper.updateById(updateDetail);
    }

    /**
     * 根据表类型获取任务明细
     */
    private ImportTaskDetailDO getTaskDetailByTableType(Long taskId, TableTypeEnum tableType) {
        LambdaQueryWrapper<ImportTaskDetailDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImportTaskDetailDO::getTaskId, taskId)
                .eq(ImportTaskDetailDO::getTableType, tableType.getType());

        return taskDetailMapper.selectOne(wrapper);
    }

    /**
     * 从文件路径重构MultipartFile对象
     * 用于重试时重新读取文件
     */
    private MultipartFile reconstructFileFromPath(String filePath) {
        // 这里需要实现从文件路径读取文件并构造MultipartFile的逻辑
        // 实际实现中可以使用Spring的MockMultipartFile
        throw new UnsupportedOperationException("需要实现文件重构逻辑");
    }

    /**
     * 计算预计剩余时间
     * <p>
     * 这是一个业务逻辑方法，基于当前进度估算剩余时间
     */
    private Long calculateEstimatedRemainingTime(int currentProgress) {
        if (currentProgress <= 0 || currentProgress >= 100) {
            return 0L;
        }

        // 简单的线性估算，实际项目中可以使用更复杂的算法
        // 假设总时间需要30分钟，根据当前进度计算剩余时间
        long totalEstimatedSeconds = 30 * 60; // 30分钟
        long remainingProgress = 100 - currentProgress;
        return (totalEstimatedSeconds * remainingProgress) / 100;
    }

    /**
     * 构建表进度列表，整合数据库信息和Redis缓存信息
     * <p>
     * 这个方法展示了如何将持久化数据和缓存数据有机结合
     */
    private List<TableProgressVO> buildTableProgressList(
            List<ImportTaskDetailDO> details,
            Map<String, TaskDetailProgressInfo> detailProgressMap) {

        return details.stream()
                .map(detail -> {
                    TaskDetailProgressInfo progressInfo = detailProgressMap.get(String.valueOf(detail.getTableType()));

                    return TableProgressVO.builder()
                            .tableType(detail.getTableType())
                            .tableName(getTableDisplayName(TableTypeEnum.valueOf(String.valueOf(detail.getTableType()))))
                            .status(detail.getStatus())
                            .progress(progressInfo != null ? progressInfo.getProgress() : detail.getProgressPercent())
                            .currentMessage(progressInfo != null ? progressInfo.getMessage() : "")
                            .totalRecords(detail.getTotalRows())
                            .successRecords(detail.getSuccessRows())
                            .failedRecords(detail.getFailedRows())
                            .startTime(detail.getStartTime())
                            .endTime(detail.getEndTime())
                            .build();
                })
                .collect(Collectors.toList());
    }
}