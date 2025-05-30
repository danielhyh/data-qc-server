package cn.iocoder.yudao.module.drug.service.batch;

import cn.iocoder.yudao.module.drug.controller.admin.batch.vo.FileExtractResult;
import cn.iocoder.yudao.module.drug.dal.dataobject.batch.ImportTaskDO;
import cn.iocoder.yudao.module.drug.dal.mysql.batch.ImportTaskDetailMapper;
import cn.iocoder.yudao.module.drug.dal.mysql.batch.ImportTaskMapper;
import cn.iocoder.yudao.module.drug.enums.DetailStatusEnum;
import cn.iocoder.yudao.module.drug.enums.TableTypeEnum;
import cn.iocoder.yudao.module.drug.enums.TaskStatusEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.FileInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.drug.enums.DrugErrorCodeConstants.FILE_READ_ERROR;
import static cn.iocoder.yudao.module.drug.enums.DrugErrorCodeConstants.ZIP_EXTRACT_FAILED;

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

    // 文件类型与表类型的映射关系
    private static final Map<String, TableTypeEnum> FILE_TYPE_MAPPING = Map.of(
            "机构基本情况", TableTypeEnum.HOSPITAL_INFO,
            "药品目录", TableTypeEnum.DRUG_CATALOG,
            "药品入库情况", TableTypeEnum.DRUG_INBOUND,
            "药品出库情况", TableTypeEnum.DRUG_OUTBOUND,
            "药品使用情况", TableTypeEnum.DRUG_USAGE
    );

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
    private DrugDataParseService dataParseService;
    @Resource
    private DrugDataImportService dataImportService;
    @Resource
    private DrugQualityControlService qualityControlService;
    @Resource
    private AsyncTaskExecutor asyncTaskExecutor;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

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
            updateTaskProgress(taskId, 10, "正在解压文件...");

            FileExtractResult extractResult = fileExtractService.extractAndValidate(taskId, file);
            if (!extractResult.isSuccess()) {
                throw exception(ZIP_EXTRACT_FAILED, extractResult.getErrorMessage());
            }

            // 创建任务明细记录
            createTaskDetails(taskId, task.getTaskNo(), extractResult.getFileInfos());
            updateTaskProgress(taskId, 20, "文件解压完成，准备导入数据...");

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
                            String.format("正在处理%s...", getTableDisplayName(tableType)));

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
            updateTaskProgress(taskId, 80, "正在执行质控检查...");
            // todo
//            QualityControlResult qcResult = qualityControlService.executeFullQualityControl(taskId);

            // 第四阶段：更新最终状态
            TaskStatusEnum finalStatus = determineFinalStatus(hasError, qcResult);
            updateTaskFinalStatus(taskId, finalStatus, totalSuccess, totalFailed);
            updateTaskProgress(taskId, 100, "任务处理完成");

            log.info("导入任务完成: taskId={}, status={}, success={}, failed={}",
                    taskId, finalStatus, totalSuccess, totalFailed);

        } catch (Exception e) {
            log.error("导入任务执行异常: taskId={}", taskId, e);
            handleTaskError(taskId, e.getMessage());
            updateTaskProgress(taskId, -1, "任务执行失败: " + e.getMessage());
        }
    }

    /**
     * 更新任务进度到Redis
     * 用于前端实时获取进度信息
     */
    private void updateTaskProgress(Long taskId, int progress, String message) {
        String key = String.format("drug:task:progress:%d", taskId);

        TaskProgressInfo progressInfo = TaskProgressInfo.builder()
                .taskId(taskId)
                .progress(progress)
                .message(message)
                .updateTime(LocalDateTime.now())
                .build();

        redisTemplate.opsForValue().set(key, progressInfo, 30, TimeUnit.MINUTES);
    }

    /**
     * 处理单个表的数据导入
     * 展示了数据处理的详细步骤和进度更新
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

        // 6. 执行表级质控 todo
//        QualityControlResult qcResult = qualityControlService.executeTableQualityControl(
//                taskId, tableType);

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
     * 避免大事务，提高性能和稳定性
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

                // 更新进度
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
        DrugImportTaskDO task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        // 查询任务明细
        List<DrugImportTaskDetailDO> details = taskDetailMapper.selectByTaskId(taskId);

        // 从Redis获取实时进度信息
        String key = String.format("drug:task:progress:%d", taskId);
        TaskProgressInfo progressInfo = (TaskProgressInfo) redisTemplate.opsForValue().get(key);

        // 构建进度信息
        return DrugImportProgressVO.builder()
                .taskId(taskId)
                .taskNo(task.getTaskNo())
                .taskName(task.getTaskName())
                .overallStatus(task.getStatus())
                .overallProgress(task.getProgressPercent())
                .currentMessage(progressInfo != null ? progressInfo.getMessage() : "")
                .startTime(task.getStartTime())
                .estimatedEndTime(calculateEstimatedEndTime(task, details))
                .totalFiles(task.getTotalFiles())
                .successFiles(task.getSuccessFiles())
                .failedFiles(task.getFailedFiles())
                .totalRecords(task.getTotalRecords())
                .successRecords(task.getSuccessRecords())
                .failedRecords(task.getFailedRecords())
                .tableProgress(buildTableProgressList(details))
                .currentStage(getCurrentStage(task.getStatus()))
                .canRetry(canRetryTask(task))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DrugImportRetryResult retryImport(Long taskId, RetryTypeEnum retryType, String fileType) {
        // 验证任务状态
        DrugImportTaskDO task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        if (!canRetryTask(task)) {
            throw new BusinessException("当前任务状态不支持重试");
        }

        log.info("开始重试导入任务: taskId={}, retryType={}, fileType={}",
                taskId, retryType, fileType);

        // 根据重试类型执行不同的重试策略
        switch (retryType) {
            case ALL:
                return retryAllTables(task);
            case FAILED:
                return retryFailedTables(task);
            case FILE_TYPE:
                return retrySpecificTable(task, fileType);
            default:
                throw new BusinessException("不支持的重试类型");
        }
    }

    // 其他辅助方法...
}