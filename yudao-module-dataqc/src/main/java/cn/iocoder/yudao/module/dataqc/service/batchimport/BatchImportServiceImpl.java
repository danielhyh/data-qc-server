package cn.iocoder.yudao.module.dataqc.service.batchimport;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import cn.iocoder.yudao.module.dataqc.controller.admin.batchimport.vo.BatchImportTaskRespVO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.batchimport.BatchImportTaskDO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.batchimport.BatchImportTaskDetailDO;
import cn.iocoder.yudao.module.dataqc.dal.mysql.batchimport.BatchImportTaskDetailMapper;
import cn.iocoder.yudao.module.dataqc.dal.mysql.batchimport.BatchImportTaskMapper;
import cn.iocoder.yudao.module.dataqc.service.drug.DrugInoutInfoService;
import cn.iocoder.yudao.module.dataqc.service.drug.DrugListService;
import cn.iocoder.yudao.module.dataqc.service.drug.DrugUseInfoService;
import cn.iocoder.yudao.module.dataqc.service.drug.HosResourceInfoService;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    @Value("${ruoyi.profile}")
    private String uploadPath;
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
    @Resource
    private FileApi fileApi;

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
     * 导入单个文件
     */
    private ImportResult importFile(BatchImportTaskDO task, String fileType, File file) throws Exception {
        log.info("开始导入文件：{}, 类型：{}", file.getName(), fileType);

        // 更新明细状态为处理中
        updateDetailStatus(task.getId(), fileType, 1, null);

        ImportResult result = new ImportResult();
        String importBatchNo = task.getTaskNo() + "_" + fileType;

        try (FileInputStream fis = new FileInputStream(file)) {
            MultipartFile multipartFile = new MockMultipartFile(
                    file.getName(),
                    file.getName(),
                    "application/vnd.ms-excel",
                    fis
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
            SysBatchImportTaskDetail detail = new SysBatchImportTaskDetail();
            detail.setTaskId(taskId);
            detail.setTaskNo(taskNo);
            detail.setFileType(entry.getKey());
            detail.setFileName(entry.getValue().getName());
            detail.setTableName(getTableName(entry.getKey()));
            detail.setStatus(0); // 待处理
            detail.setCreateTime(new Date());
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
        return "BATCH_" + DateUtils.dateTimeNow("yyyyMMddHHmmss") + "_" + IdUtil.fastSimpleUUID().substring(0, 6);
    }

    /**
     * 更新任务状态
     */
    private void updateTaskStatus(Long taskId, Integer status, String errorMsg) {
        SysBatchImportTask update = new SysBatchImportTask();
        update.setId(taskId);
        update.setStatus(status);
        if (status == 1) {
            update.setStartTime(new Date());
        } else if (status >= 2) {
            update.setEndTime(new Date());
        }
        if (StringUtils.isNotEmpty(errorMsg)) {
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

        SysBatchImportTask update = new SysBatchImportTask();
        update.setId(taskId);
        update.setStatus(status);
        update.setSuccessFiles(successFiles);
        update.setFailFiles(failFiles);
        update.setEndTime(new Date());
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
        LambdaQueryWrapper<SysBatchImportTaskDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysBatchImportTaskDetail::getTaskId, taskId)
                .eq(SysBatchImportTaskDetail::getFileType, fileType);

        SysBatchImportTaskDetail update = new SysBatchImportTaskDetail();
        update.setStatus(status);
        update.setTotalRows(result.getTotalCount());
        update.setSuccessRows(result.getSuccessCount());
        update.setFailRows(result.getFailCount());
        update.setEndTime(new Date());
        if (StringUtils.isNotEmpty(result.getMessage())) {
            update.setErrorMsg(result.getMessage());
        }

        taskDetailMapper.update(update, wrapper);
    }

    // 其他方法实现...
}

/**
 * 导入结果内部类
 */
@Data
class ImportResult {
    private boolean success = true;
    private String message;
    private int totalCount;
    private int successCount;
    private int insertCount;
    private int updateCount;
    private int failCount;

    public static ImportResult error(String message) {
        ImportResult result = new ImportResult();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }
}