package cn.iocoder.yudao.module.drug.service.batch;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.drug.controller.admin.batch.vo.FileExtractResult;
import cn.iocoder.yudao.module.drug.controller.admin.batch.vo.ImportTaskPageReqVO;
import cn.iocoder.yudao.module.drug.dal.dataobject.batch.ImportTaskDO;
import cn.iocoder.yudao.module.drug.dal.dataobject.batch.ImportTaskDetailDO;
import cn.iocoder.yudao.module.drug.dal.dataobject.batch.TaskDetailProgressInfo;
import cn.iocoder.yudao.module.drug.dal.dataobject.batch.TaskProgressInfo;
import cn.iocoder.yudao.module.drug.dal.mysql.batch.ImportTaskDetailMapper;
import cn.iocoder.yudao.module.drug.dal.mysql.batch.ImportTaskMapper;
import cn.iocoder.yudao.module.drug.dal.redis.batch.TaskProgressRedisDAO;
import cn.iocoder.yudao.module.drug.enums.DetailStatusEnum;
import cn.iocoder.yudao.module.drug.enums.TableTypeEnum;
import cn.iocoder.yudao.module.drug.enums.TaskStatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.FileInfo;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.drug.enums.DrugErrorCodeConstants.*;

/**
 * 药品数据批量导入服务实现
 * <p>
 * 设计理念：
 * 1. 统一的任务管理：所有导入操作都通过任务来管理
 * 2. 分阶段处理：解压 -> 解析 -> 导入 -> 质控
 * 3. 智能重试：支持精确重试
 * 4. 实时进度：提供多层次的进度反馈
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
    private AsyncTaskExecutor asyncTaskExecutor;
    @Resource
    private TaskProgressRedisDAO taskProgressRedisDAO;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DrugImportTaskCreateResult createImportTask(MultipartFile file, String taskName) {
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
        return DrugImportTaskCreateResult.builder()
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

            // 重构亮点：使用专门的方法更新进度，内部逻辑被完全封装
            updateTaskProgress(taskId, 10, "正在解压文件...", "EXTRACTING");

            FileExtractResult extractResult = fileExtractService.extractAndValidate(taskId, file);
            if (!extractResult.isSuccess()) {
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

                    if (result.hasError()) {
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

            // 模拟质控结果，实际项目中这里应该调用真实的质控服务
            QualityControlResult qcResult = simulateQualityControl(taskId);

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
     * @param taskId 任务ID
     * @param progress 进度百分比
     * @param message 状态描述
     * @param currentStage 当前阶段
     */
    private void updateTaskProgress(Long taskId, int progress, String message, String currentStage) {
        // 构建进度信息对象，这里可以加入更多的计算逻辑
        TaskProgressInfo progressInfo = TaskProgressInfo.builder()
                .taskId(taskId)
                .progress(progress)
                .message(message)
                .currentStage(currentStage)
                .estimatedRemainingSeconds(calculateEstimatedRemainingTime(progress))
                .updateTime(LocalDateTime.now())
                .build();

        // 重构亮点：一行代码完成复杂的Redis操作，代码变得非常清晰
        taskProgressRedisDAO.setTaskProgress(progressInfo);

        log.debug("任务进度已更新: taskId={}, progress={}%, message={}", taskId, progress, message);
    }

    /**
     * 更新任务明细进度 - 新增的细粒度控制方法
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
        if (!parseResult.isSuccess()) {
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
        QualityControlResult qcResult = simulateTableQualityControl(taskId, tableType);

        updateDetailProgress(taskId, tableType, 100,
                String.format("质控完成，通过%d条，失败%d条",
                        qcResult.getPassedCount(), qcResult.getFailedCount()));

        // 7. 确定最终状态
        DetailStatusEnum finalStatus = qcResult.isSuccess() ?
                (importResult.hasError() ? DetailStatusEnum.PARTIAL_SUCCESS : DetailStatusEnum.SUCCESS) :
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

                // 更新明细进度 - 使用新的DAO方法
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
                .hasError(failedCount > 0)
                .build();
    }

    @Override
    public DrugImportProgressVO getTaskProgress(Long taskId) {
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
        return DrugImportProgressVO.builder()
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
    public DrugImportRetryResult retryImport(Long taskId, RetryTypeEnum retryType, String fileType) {
        // 验证任务状态
        ImportTaskDO task = taskMapper.selectById(taskId);
        if (task == null) {
            throw exception(TASK_NOT_FOUND);
        }

        if (!canRetryTask(task)) {
            throw exception("当前任务状态不支持重试");
        }

        // 使用分布式锁确保重试操作的安全性
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (!taskProgressRedisDAO.tryLockTask(taskId, userId)) {
            throw exception("任务正在被其他用户操作，请稍后重试");
        }

        try {
            log.info("开始重试导入任务: taskId={}, retryType={}, fileType={}, userId={}",
                    taskId, retryType, fileType, userId);

            // 根据重试类型执行不同的重试策略
            switch (retryType) {
                case ALL:
                    return retryAllTables(task);
                case FAILED:
                    return retryFailedTables(task);
                case FILE_TYPE:
                    return retrySpecificTable(task, fileType);
                default:
                    throw exception("不支持的重试类型");
            }
        } finally {
            // 确保锁被释放
            taskProgressRedisDAO.unlockTask(taskId, userId);
        }
    }

    @Override
    public void cancelTask(Long taskId) {
        Long userId =  SecurityFrameworkUtils.getLoginUserId();

        // 尝试获取任务锁
        if (!taskProgressRedisDAO.tryLockTask(taskId, userId)) {
            throw exception("任务正在被其他用户操作，无法取消");
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

    // ==================== 私有辅助方法 ====================

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
                    TaskDetailProgressInfo progressInfo = detailProgressMap.get(detail.getTableType());

                    return TableProgressVO.builder()
                            .tableType(detail.getTableType())
                            .tableName(getTableDisplayName(TableTypeEnum.valueOf(detail.getTableType())))
                            .status(detail.getStatus())
                            .progress(progressInfo != null ? progressInfo.getProgress() : detail.getProgressPercent())
                            .currentMessage(progressInfo != null ? progressInfo.getMessage() : "")
                            .totalRecords(detail.getTotalRecords())
                            .successRecords(detail.getSuccessRecords())
                            .failedRecords(detail.getFailedRecords())
                            .startTime(detail.getStartTime())
                            .endTime(detail.getEndTime())
                            .build();
                })
                .collect(Collectors.toList());
    }

}