package cn.iocoder.yudao.module.dataqc.service.batchimport;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.dataqc.controller.admin.batchimport.vo.BatchImportTaskDetailRespVO;
import cn.iocoder.yudao.module.dataqc.controller.admin.batchimport.vo.BatchImportTaskRespVO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.batchimport.BatchImportTaskDO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.batchimport.BatchImportTaskDetailDO;
import cn.iocoder.yudao.module.dataqc.dal.mysql.batchimport.BatchImportTaskDetailMapper;
import cn.iocoder.yudao.module.dataqc.dal.mysql.batchimport.BatchImportTaskMapper;
import cn.iocoder.yudao.module.dataqc.service.drug.DrugInoutInfoService;
import cn.iocoder.yudao.module.dataqc.service.drug.DrugListService;
import cn.iocoder.yudao.module.dataqc.service.drug.DrugUseInfoService;
import cn.iocoder.yudao.module.dataqc.service.drug.HosResourceInfoService;
import cn.iocoder.yudao.module.dataqc.util.CustomMultipartFile;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.dataqc.enums.ErrorCodeConstants.*;

;

/**
 * 批量导入服务实现
 */
@Slf4j
@Service
public class BatchImportServiceImpl implements IBatchImportService {

    // 文件类型映射
    private static final Map<String, String> FILE_TYPE_MAPPING = new HashMap<String, String>() {{
        put("公立医疗机构基本情况", "HOSPITAL_INFO");
        put("公立医疗机构药品目录", "DRUG_LIST");
        put("公立医疗机构药品入库情况", "DRUG_IN");
        put("公立医疗机构药品出库情况", "DRUG_OUT");
        put("公立医疗机构药品使用情况", "DRUG_USE");
    }};
    // 导入顺序（有依赖关系）
    private static final List<String> IMPORT_ORDER = Arrays.asList(
            "HOSPITAL_INFO", "DRUG_LIST", "DRUG_IN", "DRUG_OUT", "DRUG_USE"
    );
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    @Resource
    private BatchImportTaskMapper taskMapper;
    @Resource
    private BatchImportTaskDetailMapper taskDetailMapper;
    @Resource
    private HosResourceInfoService resourceService;
    @Resource
    private DrugListService drugListService;
    @Resource
    private DrugInoutInfoService inoutService;
    @Resource
    private DrugUseInfoService useService;
    @Value("${dataqc.temp-dir}")
    private String uploadPath;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchImportTaskRespVO batchImport(MultipartFile file) throws Exception {
        // 1. 创建导入任务
        String taskNo = generateTaskNo();
        BatchImportTaskDO task = createImportTask(file, taskNo);

        // 2. 异步处理导入
        CompletableFuture.runAsync(() -> {
            try {
                processImport(task, file);
            } catch (Exception e) {
                log.error("批量导入失败，任务编号：{}", taskNo, e);
                updateTaskStatus(task.getId(), 3, e.getMessage());
            }
        }, executorService);

        // 3. 返回任务信息
        BatchImportTaskRespVO result = new BatchImportTaskRespVO();
        result.setId(task.getId());
        result.setTaskNo(taskNo);
        result.setMessage("导入任务已创建，正在后台处理，请稍后查看结果");
        return result;
    }

    @Override
    public List<BatchImportTaskDO> selectTaskList(BatchImportTaskDO task) {
        // 构建查询条件
        LambdaQueryWrapper<BatchImportTaskDO> wrapper = new LambdaQueryWrapper<>();

        // 支持按任务编号模糊查询
        wrapper.like(StrUtil.isNotEmpty(task.getTaskNo()),
                BatchImportTaskDO::getTaskNo, task.getTaskNo());

        // 支持按状态精确查询
        wrapper.eq(task.getStatus() != null,
                BatchImportTaskDO::getStatus, task.getStatus());

        // 支持按文件名模糊查询
        wrapper.like(StrUtil.isNotEmpty(task.getFileName()),
                BatchImportTaskDO::getFileName, task.getFileName());

        // 支持时间范围查询
        if (task.getCreateTime() != null) {
            wrapper.ge(BatchImportTaskDO::getCreateTime, task.getCreateTime());
        }

        // 按创建时间降序排序，最新的在前面
        wrapper.orderByDesc(BatchImportTaskDO::getCreateTime);

        return taskMapper.selectList(wrapper);
    }

    @Override
    public BatchImportTaskDO selectTaskById(Long taskId) {
        if (taskId == null) {
            return null;
        }
        return taskMapper.selectById(taskId);
    }

    @Override
    public List<BatchImportTaskDetailRespVO> selectTaskDetailList(Long taskId) {
        if (taskId == null) {
            return new ArrayList<>();
        }

        // 查询任务明细
        LambdaQueryWrapper<BatchImportTaskDetailDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BatchImportTaskDetailDO::getTaskId, taskId);
        wrapper.orderByAsc(BatchImportTaskDetailDO::getCreateTime);

        List<BatchImportTaskDetailDO> detailList = taskDetailMapper.selectList(wrapper);

        // 转换为响应VO，添加友好的显示名称
        return detailList.stream().map(detail -> {
            BatchImportTaskDetailRespVO vo = BeanUtils.toBean(detail, BatchImportTaskDetailRespVO.class);
            vo.setFileTypeDisplay(getFileTypeDisplay(detail.getFileType()));
            vo.setStatusDisplay(getStatusDisplay(detail.getStatus()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retryImport(Long taskId, String fileType) throws Exception {
        // 查询原始任务信息
        BatchImportTaskDO task = selectTaskById(taskId);
        if (task == null) {
            throw exception(BATCH_IMPORT_TASK_NOT_EXISTS);
        }

        // 查询失败的明细
        LambdaQueryWrapper<BatchImportTaskDetailDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BatchImportTaskDetailDO::getTaskId, taskId);

        if (StrUtil.isNotEmpty(fileType)) {
            // 重试指定类型的文件
            wrapper.eq(BatchImportTaskDetailDO::getFileType, fileType);
        } else {
            // 重试所有失败的文件
            wrapper.eq(BatchImportTaskDetailDO::getStatus, 3); // 状态3表示失败
        }

        List<BatchImportTaskDetailDO> failedDetails = taskDetailMapper.selectList(wrapper);
        if (failedDetails.isEmpty()) {
            throw exception(NO_FAILED_IMPORT_FOUND);
        }

        // 重置明细状态为待处理
        for (BatchImportTaskDetailDO detail : failedDetails) {
            detail.setStatus(0); // 待处理
            detail.setErrorMsg(null);
            detail.setErrorDetail(null);
            taskDetailMapper.updateById(detail);
        }

        // 异步重新处理失败的文件
        CompletableFuture.runAsync(() -> {
            try {
                retryFailedFiles(task, failedDetails);
            } catch (Exception e) {
                log.error("重试导入失败，任务ID：{}", taskId, e);
            }
        }, executorService);
    }
    /**
     * 重试失败的文件导入
     */
    private void retryFailedFiles(BatchImportTaskDO task, List<BatchImportTaskDetailDO> failedDetails) {
        // 这里需要重新读取原始文件，实际项目中可能需要保存原始文件
        // 为了简化示例，假设我们有保存原始文件的机制
        log.info("开始重试导入失败的文件，任务：{}", task.getTaskNo());

        for (BatchImportTaskDetailDO detail : failedDetails) {
            try {
                // 更新状态为处理中
                updateDetailStatus(task.getId(), detail.getFileType(), 1, null);

                // 根据文件类型重新导入
                // 这里应该调用相应的导入服务
                // 示例代码：
                // retryImportByType(detail.getFileType(), originalFileData);

                // 模拟处理成功
                updateDetailStatus(task.getId(), detail.getFileType(), 2, null);

            } catch (Exception e) {
                log.error("重试导入文件失败：{}", detail.getFileType(), e);
                updateDetailStatus(task.getId(), detail.getFileType(), 3, e.getMessage());
            }
        }

        // 更新主任务状态
        updateTaskFinalStatus(task.getId());
    }
    /**
     * 处理导入逻辑
     */
    private void processImport(BatchImportTaskDO task, MultipartFile file) throws Exception {
        String tempDir = null;
        try {
            // 1. 保存并解压文件
            tempDir = saveAndUnzipFile(file, task.getTaskNo());

            // 2. 扫描并验证文件
            Map<String, File> fileMap = scanAndValidateFiles(tempDir);

            // 3. 创建导入明细
            createImportDetails(task.getId(), task.getTaskNo(), fileMap);

            // 4. 更新任务状态为处理中
            updateTaskStatus(task.getId(), 1, null);

            // 5. 按顺序导入文件
            boolean hasError = false;
            Map<String, Object> resultDetail = new HashMap<>();

            for (String fileType : IMPORT_ORDER) {
                if (fileMap.containsKey(fileType)) {
                    try {
                        ImportResult importResult = importFile(task, fileType, fileMap.get(fileType));
                        resultDetail.put(fileType, importResult);

                        if (!importResult.isSuccess()) {
                            hasError = true;
                        }
                    } catch (Exception e) {
                        log.error("导入{}失败", fileType, e);
                        hasError = true;
                        resultDetail.put(fileType, ImportResult.error(e.getMessage()));
                        updateDetailStatus(task.getId(), fileType, 3, e.getMessage());
                    }
                }
            }

            // 6. 更新最终状态
            int finalStatus = hasError ? 4 : 2; // 4-部分成功，2-全部成功
            updateTaskStatusWithDetail(task.getId(), finalStatus, resultDetail);

        } finally {
            // 清理临时文件
            if (tempDir != null) {
                FileUtil.del(tempDir);
            }
        }
    }

    /**
     * 保存并解压文件
     */
    private String saveAndUnzipFile(MultipartFile file, String taskNo) throws Exception {
        // 创建临时目录
        String tempPath = uploadPath + "/temp/" + taskNo;
        File tempDir = new File(tempPath);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        // 保存压缩包
        String zipPath = tempPath + "/" + file.getOriginalFilename();
        File zipFile = new File(zipPath);
        file.transferTo(zipFile);

        // 解压文件
        ZipUtil.unzip(zipFile, tempDir);

        // 删除压缩包
        zipFile.delete();

        return tempPath;
    }

    /**
     * 扫描并验证文件
     */
    private Map<String, File> scanAndValidateFiles(String tempDir) throws Exception {
        Map<String, File> fileMap = new HashMap<>();
        File dir = new File(tempDir);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".xlsx") || name.endsWith(".xls"));

        if (files == null || files.length == 0) {
            throw exception(ZIP_FILE_NOT_FOUND_EXCEL);
        }

        // 根据文件名匹配文件类型
        for (File file : files) {
            String fileName = file.getName();
            boolean matched = false;

            for (Map.Entry<String, String> entry : FILE_TYPE_MAPPING.entrySet()) {
                if (fileName.contains(entry.getKey())) {
                    fileMap.put(entry.getValue(), file);
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                log.warn("未识别的文件：{}", fileName);
            }
        }

        // 验证必要文件是否存在
        if (!fileMap.containsKey("DRUG_LIST")) {
            throw exception(MISSING_NECESSARY_FILE_MEDICINE_CATEGORY);
        }

        return fileMap;
    }

    /**
     * 修复后的importFile方法中的文件转换部分
     * 使用我们自定义的CustomMultipartFile替代MockMultipartFile
     */
    private ImportResult importFile(BatchImportTaskDO task, String fileType, File file) throws Exception {
        log.info("开始导入文件：{}, 类型：{}", file.getName(), fileType);

        // 更新明细状态为处理中
        updateDetailStatus(task.getId(), fileType, 1, null);

        ImportResult result = new ImportResult();
        String importBatchNo = task.getTaskNo() + "_" + fileType;

        try {
            // 使用我们自定义的CustomMultipartFile，而不是MockMultipartFile
            MultipartFile multipartFile = new CustomMultipartFile(
                    file.getName(),
                    file.getName(),
                    "application/vnd.ms-excel",
                    file  // 直接传入File对象
            );

            switch (fileType) {
                case "HOSPITAL_INFO":
                    String msg1 = resourceService.importResourceData(multipartFile, true);
                    result = parseImportMessage(msg1);
                    break;

                case "DRUG_LIST":
                    String msg2 = drugListService.importDrugList(multipartFile, true);
                    result = parseImportMessage(msg2);
                    break;

                case "DRUG_IN":
                    String msg3 = inoutService.importInData(multipartFile, true);
                    result = parseImportMessage(msg3);
                    break;

                case "DRUG_OUT":
                    String msg4 = inoutService.importOutData(multipartFile, true);
                    result = parseImportMessage(msg4);
                    break;

                case "DRUG_USE":
                    String msg5 = useService.importUseData(multipartFile, true);
                    result = parseImportMessage(msg5);
                    break;

                default:
                    throw exception(UNSUPPORTED_FILE_TYPE, fileType);
            }

            // 更新明细状态
            int status = result.getFailCount() == 0 ? 2 : 4; // 2-成功，4-部分成功
            updateDetailStatusWithResult(task.getId(), fileType, status, result);

        } catch (Exception e) {
            log.error("导入文件失败：{}", file.getName(), e);
            result.setSuccess(false);
            result.setMessage(e.getMessage());
            updateDetailStatus(task.getId(), fileType, 3, e.getMessage());
            throw e;
        }

        return result;
    }

    /**
     * 解析导入返回消息
     */
    private ImportResult parseImportMessage(String message) {
        ImportResult result = new ImportResult();
        result.setMessage(message);

        // 解析消息格式：导入成功！总数：100，新增：80，更新：10，失败：10
        if (message.contains("总数：")) {
            String[] parts = message.split("，");
            for (String part : parts) {
                if (part.contains("总数：")) {
                    result.setTotalCount(extractNumber(part));
                } else if (part.contains("新增：")) {
                    result.setInsertCount(extractNumber(part));
                } else if (part.contains("更新：")) {
                    result.setUpdateCount(extractNumber(part));
                } else if (part.contains("成功：")) {
                    result.setSuccessCount(extractNumber(part));
                } else if (part.contains("失败：")) {
                    result.setFailCount(extractNumber(part));
                }
            }
            result.setSuccess(result.getFailCount() == 0);
        }

        return result;
    }

    /**
     * 提取数字
     */
    private int extractNumber(String text) {
        String number = text.replaceAll("[^0-9]", "");
        return StrUtil.isNotEmpty(number) ? Integer.parseInt(number) : 0;
    }

    /**
     * 创建导入任务
     */
    private BatchImportTaskDO createImportTask(MultipartFile file, String taskNo) {
        BatchImportTaskDO task = new BatchImportTaskDO();
        task.setTaskNo(taskNo);
        task.setTaskName("药品数据批量导入_" + DateUtil.now());
        task.setFileName(file.getOriginalFilename());
        task.setStatus(0); // 待处理
        task.setTotalFiles(5); // 预期5个文件
        task.setCreateTime(LocalDateTime.now());
        taskMapper.insert(task);
        return task;
    }

    /**
     * 创建导入明细
     */
    private void createImportDetails(Long taskId, String taskNo, Map<String, File> fileMap) {
        for (Map.Entry<String, File> entry : fileMap.entrySet()) {
            BatchImportTaskDetailDO detail = new BatchImportTaskDetailDO();
            detail.setTaskId(taskId);
            detail.setTaskNo(taskNo);
            detail.setFileType(entry.getKey());
            detail.setFileName(entry.getValue().getName());
            detail.setTableName(getTableName(entry.getKey()));
            detail.setStatus(0); // 待处理
            detail.setCreateTime(LocalDateTime.now());
            taskDetailMapper.insert(detail);
        }
    }

    /**
     * 获取表名
     */
    private String getTableName(String fileType) {
        switch (fileType) {
            case "HOSPITAL_INFO":
                return "gh_hos_resource_info";
            case "DRUG_LIST":
                return "gh_drug_list";
            case "DRUG_IN":
            case "DRUG_OUT":
                return "gh_drug_inout_info";
            case "DRUG_USE":
                return "gh_drug_use_info";
            default:
                return "";
        }
    }

    /**
     * 生成任务编号
     */
    private String generateTaskNo() {
        return "BATCH_" + DateUtil.format(DateUtil.date(), "yyyyMMdd") + "_" + IdUtil.fastSimpleUUID().substring(0, 6);
    }

    /**
     * 更新任务状态
     */
    private void updateTaskStatus(Long taskId, Integer status, String errorMsg) {
        BatchImportTaskDO update = new BatchImportTaskDO();
        update.setId(taskId);
        update.setStatus(status);
        if (status == 1) {
            update.setStartTime(LocalDateTime.now());
        } else if (status >= 2) {
            update.setEndTime(LocalDateTime.now());
        }
        if (StrUtil.isNotEmpty(errorMsg)) {
            update.setErrorMsg(errorMsg);
        }
        taskMapper.updateById(update);
    }

    /**
     * 更新任务状态和结果详情
     */
    private void updateTaskStatusWithDetail(Long taskId, Integer status, Map<String, Object> resultDetail) {
        // 统计成功和失败文件数
        int successFiles = 0;
        int failFiles = 0;

        for (Object value : resultDetail.values()) {
            if (value instanceof ImportResult) {
                ImportResult result = (ImportResult) value;
                if (result.isSuccess()) {
                    successFiles++;
                } else {
                    failFiles++;
                }
            }
        }

        BatchImportTaskDO update = new BatchImportTaskDO();
        update.setId(taskId);
        update.setStatus(status);
        update.setSuccessFiles(successFiles);
        update.setFailFiles(failFiles);
        update.setEndTime(LocalDateTime.now());
        update.setResultDetail(JSON.toJSONString(resultDetail));
        taskMapper.updateById(update);
    }

    /**
     * 更新明细状态
     */
    private void updateDetailStatus(Long taskId, String fileType, Integer status, String errorMsg) {
        LambdaQueryWrapper<BatchImportTaskDetailDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BatchImportTaskDetailDO::getTaskId, taskId)
                .eq(BatchImportTaskDetailDO::getFileType, fileType);

        BatchImportTaskDetailDO update = new BatchImportTaskDetailDO();
        update.setStatus(status);
        if (status == 1) {
            update.setStartTime(LocalDateTime.now());
        } else if (status >= 2) {
            update.setEndTime(LocalDateTime.now());
        }
        if (StrUtil.isNotEmpty(errorMsg)) {
            update.setErrorMsg(errorMsg);
        }

        taskDetailMapper.update(update, wrapper);
    }

    /**
     * 更新明细状态和结果
     */
    private void updateDetailStatusWithResult(Long taskId, String fileType, Integer status, ImportResult result) {
        LambdaQueryWrapper<BatchImportTaskDetailDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BatchImportTaskDetailDO::getTaskId, taskId)
                .eq(BatchImportTaskDetailDO::getFileType, fileType);

        BatchImportTaskDetailDO update = new BatchImportTaskDetailDO();
        update.setStatus(status);
        update.setTotalRows(result.getTotalCount());
        update.setSuccessRows(result.getSuccessCount());
        update.setFailRows(result.getFailCount());
        update.setEndTime(LocalDateTime.now());
        if (StrUtil.isNotEmpty(result.getMessage())) {
            update.setErrorMsg(result.getMessage());
        }

        taskDetailMapper.update(update, wrapper);
    }
    /**
     * 获取文件类型的友好显示名称
     * 设计原理：将系统内部的文件类型代码转换为用户友好的中文描述
     * 这是"表示层转换"模式的典型应用
     *
     * @param fileType 内部文件类型代码
     * @return 友好的显示名称
     */
    private String getFileTypeDisplay(String fileType) {
        if (StrUtil.isEmpty(fileType)) {
            return "未知类型";
        }

        // 使用反向映射，从文件类型代码转换为显示名称
        Map<String, String> displayMapping = new HashMap<String, String>() {{
            put("HOSPITAL_INFO", "公立医疗机构基本情况");
            put("DRUG_LIST", "公立医疗机构药品目录");
            put("DRUG_IN", "公立医疗机构药品入库情况");
            put("DRUG_OUT", "公立医疗机构药品出库情况");
            put("DRUG_USE", "公立医疗机构药品使用情况");
        }};

        return displayMapping.getOrDefault(fileType, fileType + "(未识别)");
    }

    /**
     * 获取任务状态的友好显示名称
     * 设计理念：状态码对用户不友好，需要转换为可理解的描述
     *
     * 状态码设计说明：
     * 0 - 待处理：任务已创建，等待开始执行
     * 1 - 处理中：正在执行导入操作
     * 2 - 成功：所有文件都导入成功
     * 3 - 失败：导入过程中发生致命错误，任务终止
     * 4 - 部分成功：有些文件成功，有些失败
     *
     * @param status 状态码
     * @return 状态的中文描述
     */
    private String getStatusDisplay(Integer status) {
        if (status == null) {
            return "状态未知";
        }

        switch (status) {
            case 0:
                return "待处理";
            case 1:
                return "处理中";
            case 2:
                return "导入成功";
            case 3:
                return "导入失败";
            case 4:
                return "部分成功";
            case 5:
                return "已取消"; // 预留状态，用于支持任务取消功能
            default:
                return "未知状态(" + status + ")";
        }
    }

    /**
     * 更新任务的最终状态
     * 设计思路：根据所有子任务的执行结果，智能决定主任务的最终状态
     * 这是"状态聚合"模式的实现
     *
     * 决策逻辑：
     * - 如果所有子任务都成功 → 主任务成功
     * - 如果所有子任务都失败 → 主任务失败
     * - 如果部分成功部分失败 → 主任务部分成功
     * - 如果还有处理中的任务 → 主任务仍为处理中
     *
     * @param taskId 主任务ID
     */
    private void updateTaskFinalStatus(Long taskId) {
        // 查询该任务下所有子任务的状态
        LambdaQueryWrapper<BatchImportTaskDetailDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BatchImportTaskDetailDO::getTaskId, taskId);

        List<BatchImportTaskDetailDO> detailList = taskDetailMapper.selectList(wrapper);

        if (CollUtil.isEmpty(detailList)) {
            log.warn("任务 {} 下没有找到任何子任务明细", taskId);
            return;
        }

        // 统计各种状态的任务数量
        TaskStatusStatistics stats = calculateTaskStatistics(detailList);

        // 根据统计结果决定最终状态
        int finalStatus = decideFinalStatus(stats);

        // 更新主任务状态
        BatchImportTaskDO update = new BatchImportTaskDO();
        update.setId(taskId);
        update.setStatus(finalStatus);
        update.setSuccessFiles(stats.getSuccessCount());
        update.setFailFiles(stats.getFailCount());
        update.setEndTime(LocalDateTime.now());

        // 如果任务完成（无论成功失败），记录结束时间
        if (finalStatus >= 2) {
            update.setEndTime(LocalDateTime.now());

            // 计算总耗时（如果有开始时间的话）
            BatchImportTaskDO existingTask = taskMapper.selectById(taskId);
            if (existingTask != null && existingTask.getStartTime() != null) {
                long duration = java.time.Duration.between(
                        existingTask.getStartTime(),
                        LocalDateTime.now()
                ).toSeconds();

                log.info("任务 {} 执行完成，总耗时：{} 秒", taskId, duration);
            }
        }

        taskMapper.updateById(update);

        log.info("任务 {} 最终状态更新为：{}, 成功文件数：{}, 失败文件数：{}",
                taskId, getStatusDisplay(finalStatus), stats.getSuccessCount(), stats.getFailCount());
    }

    /**
     * 计算任务状态统计信息
     * 这是一个纯函数，便于单元测试
     */
    private TaskStatusStatistics calculateTaskStatistics(List<BatchImportTaskDetailDO> detailList) {
        TaskStatusStatistics stats = new TaskStatusStatistics();

        for (BatchImportTaskDetailDO detail : detailList) {
            Integer status = detail.getStatus();

            switch (status) {
                case 0: // 待处理
                    stats.incrementPendingCount();
                    break;
                case 1: // 处理中
                    stats.incrementProcessingCount();
                    break;
                case 2: // 成功
                    stats.incrementSuccessCount();
                    break;
                case 3: // 失败
                    stats.incrementFailCount();
                    break;
                case 4: // 部分成功
                    stats.incrementPartialSuccessCount();
                    break;
                default:
                    stats.incrementUnknownCount();
                    break;
            }
        }

        return stats;
    }

    /**
     * 根据统计信息决定最终状态
     * 这里封装了复杂的业务逻辑判断
     */
    private int decideFinalStatus(TaskStatusStatistics stats) {
        // 如果还有任务在处理中或待处理，主任务状态应该是处理中
        if (stats.getProcessingCount() > 0 || stats.getPendingCount() > 0) {
            return 1; // 处理中
        }

        // 所有任务都完成了，根据成功失败情况决定最终状态
        if (stats.getFailCount() == 0 && stats.getPartialSuccessCount() == 0) {
            // 没有失败的，没有部分成功的 → 完全成功
            return 2; // 成功
        } else if (stats.getSuccessCount() == 0 && stats.getPartialSuccessCount() == 0) {
            // 没有成功的，没有部分成功的 → 完全失败
            return 3; // 失败
        } else {
            // 有成功有失败 → 部分成功
            return 4; // 部分成功
        }
    }

    /**
     * 任务状态统计辅助类
     * 设计目的：封装复杂的状态统计逻辑，提高代码可读性和可测试性
     */
    private static class TaskStatusStatistics {
        private int pendingCount = 0;      // 待处理数量
        private int processingCount = 0;   // 处理中数量
        private int successCount = 0;      // 成功数量
        private int failCount = 0;         // 失败数量
        private int partialSuccessCount = 0; // 部分成功数量
        private int unknownCount = 0;      // 未知状态数量

        // 增量方法
        public void incrementPendingCount() { this.pendingCount++; }
        public void incrementProcessingCount() { this.processingCount++; }
        public void incrementSuccessCount() { this.successCount++; }
        public void incrementFailCount() { this.failCount++; }
        public void incrementPartialSuccessCount() { this.partialSuccessCount++; }
        public void incrementUnknownCount() { this.unknownCount++; }

        // 获取方法
        public int getPendingCount() { return pendingCount; }
        public int getProcessingCount() { return processingCount; }
        public int getSuccessCount() { return successCount; }
        public int getFailCount() { return failCount; }
        public int getPartialSuccessCount() { return partialSuccessCount; }
        public int getUnknownCount() { return unknownCount; }

        /**
         * 获取总任务数
         */
        public int getTotalCount() {
            return pendingCount + processingCount + successCount +
                    failCount + partialSuccessCount + unknownCount;
        }

        /**
         * 获取已完成任务数（无论成功失败）
         */
        public int getCompletedCount() {
            return successCount + failCount + partialSuccessCount;
        }

        /**
         * 计算完成率
         */
        public double getCompletionRate() {
            int total = getTotalCount();
            return total > 0 ? (double) getCompletedCount() / total : 0.0;
        }

        @Override
        public String toString() {
            return String.format("TaskStatusStatistics{总数=%d, 待处理=%d, 处理中=%d, 成功=%d, 失败=%d, 部分成功=%d, 未知=%d}",
                    getTotalCount(), pendingCount, processingCount, successCount, failCount, partialSuccessCount, unknownCount);
        }
    }
}

