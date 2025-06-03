package cn.iocoder.yudao.module.drug.controller.admin.batch;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.drug.controller.admin.batch.vo.*;
import cn.iocoder.yudao.module.drug.dal.dataobject.batch.ImportTaskDO;
import cn.iocoder.yudao.module.drug.dal.dataobject.batch.ImportTaskDetailDO;
import cn.iocoder.yudao.module.drug.enums.RetryTypeEnum;
import cn.iocoder.yudao.module.drug.service.batch.DrugBatchImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.IMPORT;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.OTHER;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 药品数据批量导入控制器
 * 
 * 设计理念：
 * 1. 职责分离：专注于批量导入的业务流程，与任务管理控制器分离
 * 2. 用户体验：提供完整的导入生命周期管理API
 * 3. 安全性：严格的权限控制和参数验证
 * 4. 可观测性：详细的操作日志记录
 * 
 * @author yourname
 * @since 2024-05-29
 */
@Tag(name = "管理后台 - 药品数据批量导入")
@RestController
@RequestMapping("/drug/batch-import")
@Validated
@Slf4j
public class DrugBatchImportController {

    @Resource
    private DrugBatchImportService drugBatchImportService;

    /**
     * 创建批量导入任务
     * 
     * 核心业务接口，支持压缩包上传和任务创建
     */
    @PostMapping(value = "/create-task", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "创建药品数据批量导入任务", 
               description = "上传压缩包文件，创建批量导入任务并开始异步处理")
    @PreAuthorize("@ss.hasPermission('drug:batch-import:create')")
    @ApiAccessLog(operateType = IMPORT)
    public CommonResult<ImportTaskCreateResult> createImportTask(
            @RequestPart("file") MultipartFile file,
            @RequestParam("taskName") String taskName,
            @RequestParam(value = "description", required = false) String description) {
        
        log.info("收到批量导入请求: taskName={}, fileName={}, fileSize={}", 
                taskName, file.getOriginalFilename(), file.getSize());
        
        ImportTaskCreateResult result = drugBatchImportService.createImportTask(file, taskName);
        
        log.info("批量导入任务创建成功: taskId={}, taskNo={}", 
                result.getTaskId(), result.getTaskNo());
        
        return success(result);
    }

    /**
     * 获取任务详细信息
     * 
     * 提供任务的完整状态信息，包括所有明细
     */
    @GetMapping("/task-detail/{taskId}")
    @Operation(summary = "获取导入任务详细信息")
    @Parameter(name = "taskId", description = "任务ID", required = true)
    @PreAuthorize("@ss.hasPermission('drug:batch-import:query')")
    public CommonResult<ImportTaskDetailRespVO> getTaskDetail(@PathVariable("taskId") Long taskId) {
        
        log.debug("查询任务详情: taskId={}", taskId);
        
        ImportTaskDetailDO taskDetail = drugBatchImportService.getTaskDetail(taskId);
        ImportTaskDetailRespVO result = BeanUtils.toBean(taskDetail, ImportTaskDetailRespVO.class);
        
        return success(result);
    }

    /**
     * 获取任务实时进度
     * 
     * 高频调用接口，提供实时进度信息
     */
    @GetMapping("/task-progress/{taskId}")
    @Operation(summary = "获取导入任务实时进度", 
               description = "获取任务的实时处理进度，支持前端轮询调用")
    @Parameter(name = "taskId", description = "任务ID", required = true)
    @PreAuthorize("@ss.hasPermission('drug:batch-import:query')")
    public CommonResult<ImportProgressVO> getTaskProgress(@PathVariable("taskId") Long taskId) {
        
        ImportProgressVO progress = drugBatchImportService.getTaskProgress(taskId);
        return success(progress);
    }

    /**
     * 重试失败任务
     * 
     * 支持多种重试策略的智能重试机制
     */
    @PostMapping("/retry-task")
    @Operation(summary = "重试失败的导入任务", 
               description = "支持全部重试、仅失败部分重试、指定文件类型重试等多种策略")
    @PreAuthorize("@ss.hasPermission('drug:batch-import:retry')")
    @ApiAccessLog(operateType = IMPORT)
    public CommonResult<ImportRetryResult> retryImportTask(@Valid @RequestBody ImportRetryReqVO retryReqVO) {
        
        log.info("收到任务重试请求: taskId={}, retryType={}, fileType={}", 
                retryReqVO.getTaskId(), retryReqVO.getRetryType(), retryReqVO.getFileType());
        
        RetryTypeEnum retryType = RetryTypeEnum.valueOf(retryReqVO.getRetryType());
        ImportRetryResult result = drugBatchImportService.retryImport(
                retryReqVO.getTaskId(), retryType, retryReqVO.getFileType());
        
        log.info("任务重试已启动: taskId={}, retryBatchNo={}", 
                retryReqVO.getTaskId(), result.getRetryBatchNo());
        
        return success(result);
    }

    /**
     * 取消正在进行的任务
     * 
     * 安全地取消任务并清理相关资源
     */
    @PostMapping("/cancel-task/{taskId}")
    @Operation(summary = "取消正在进行的导入任务")
    @Parameter(name = "taskId", description = "任务ID", required = true)
    @PreAuthorize("@ss.hasPermission('drug:batch-import:cancel')")
    @ApiAccessLog(operateType = OTHER)
    public CommonResult<Boolean> cancelTask(@PathVariable("taskId") Long taskId) {
        
        log.info("收到任务取消请求: taskId={}", taskId);
        
        drugBatchImportService.cancelTask(taskId);
        
        log.info("任务已成功取消: taskId={}", taskId);
        return success(true);
    }

    /**
     * 分页查询导入任务列表
     * 
     * 支持多维度筛选的任务列表查询
     */
    @GetMapping("/task-page")
    @Operation(summary = "分页查询批量导入任务列表")
    @PreAuthorize("@ss.hasPermission('drug:batch-import:query')")
    public CommonResult<PageResult<ImportTaskRespVO>> getTaskPage(@Valid ImportTaskPageReqVO pageReqVO) {
        
        log.debug("查询任务列表: pageNo={}, pageSize={}, status={}", 
                pageReqVO.getPageNo(), pageReqVO.getPageSize(), pageReqVO.getStatus());
        
        PageResult<ImportTaskDO> pageResult = drugBatchImportService.getTaskPage(pageReqVO);
        PageResult<ImportTaskRespVO> result = BeanUtils.toBean(pageResult, ImportTaskRespVO.class);
        
        return success(result);
    }

//    /**
//     * 获取导入模板
//     *
//     * 提供标准的Excel导入模板下载
//     */
//    @GetMapping("/download-template")
//    @Operation(summary = "下载导入模板",
//               description = "下载包含所有必需文件的标准导入模板")
//    @PreAuthorize("@ss.hasPermission('drug:batch-import:template')")
//    @ApiAccessLog(operateType = EXPORT)
//    public CommonResult<TemplateDownloadVO> downloadTemplate(
//            @RequestParam(value = "templateType", defaultValue = "STANDARD") String templateType) {
//
//        log.info("请求下载导入模板: templateType={}", templateType);
//
//        // 实际实现中，这里应该生成模板文件并返回下载链接
//        TemplateDownloadVO result = TemplateDownloadVO.builder()
//                .templateName("药品数据导入模板.zip")
//                .downloadUrl("/templates/drug-import-template.zip")
//                .templateVersion("v1.0")
//                .description("包含5个Excel文件的标准导入模板")
//                .fileCount(5)
//                .templateSize("2.5MB")
//                .build();
//
//        return success(result);
//    }
//
//    /**
//     * 验证压缩包文件
//     *
//     * 在实际上传前验证文件格式和内容
//     */
//    @PostMapping(value = "/validate-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    @Operation(summary = "验证导入文件",
//               description = "验证压缩包格式和内容是否符合导入要求")
//    @PreAuthorize("@ss.hasPermission('drug:batch-import:validate')")
//    public CommonResult<FileValidationResult> validateImportFile(@RequestPart("file") MultipartFile file) {
//
//        log.info("收到文件验证请求: fileName={}, fileSize={}",
//                file.getOriginalFilename(), file.getSize());
//
//        // 实际实现中，这里应该调用文件验证服务
//        FileValidationResult result = FileValidationResult.builder()
//                .valid(true)
//                .fileName(file.getOriginalFilename())
//                .fileSize(file.getSize())
//                .expectedFileCount(5)
//                .actualFileCount(5)
//                .validationMessage("文件验证通过")
//                .build();
//
//        return success(result);
//    }
//
//    /**
//     * 获取导入统计信息
//     *
//     * 提供导入操作的统计分析数据
//     */
//    @GetMapping("/statistics")
//    @Operation(summary = "获取导入统计信息")
//    @PreAuthorize("@ss.hasPermission('drug:batch-import:statistics')")
//    public CommonResult<ImportStatisticsVO> getImportStatistics(
//            @RequestParam(value = "startDate", required = false) String startDate,
//            @RequestParam(value = "endDate", required = false) String endDate) {
//
//        log.debug("查询导入统计: startDate={}, endDate={}", startDate, endDate);
//
//        // 实际实现中，这里应该调用统计服务
//        ImportStatisticsVO result = ImportStatisticsVO.builder()
//                .totalTasks(120)
//                .successTasks(108)
//                .failedTasks(8)
//                .partialSuccessTasks(4)
//                .successRate(90.0)
//                .averageProcessingTime(342) // 秒
//                .totalRecordsProcessed(1250000L)
//                .build();
//
//        return success(result);
//    }
//
//    /**
//     * 获取任务日志
//     *
//     * 提供详细的任务执行日志信息
//     */
//    @GetMapping("/task-logs/{taskId}")
//    @Operation(summary = "获取任务执行日志")
//    @Parameter(name = "taskId", description = "任务ID", required = true)
//    @PreAuthorize("@ss.hasPermission('drug:batch-import:logs')")
//    public CommonResult<TaskLogVO> getTaskLogs(
//            @PathVariable("taskId") Long taskId,
//            @RequestParam(value = "logLevel", defaultValue = "INFO") String logLevel) {
//
//        log.debug("查询任务日志: taskId={}, logLevel={}", taskId, logLevel);
//
//        // 实际实现中，这里应该调用日志服务
//        TaskLogVO result = TaskLogVO.builder()
//                .taskId(taskId)
//                .logs("任务执行日志内容...")
//                .logLevel(logLevel)
//                .totalLines(1500)
//                .lastUpdateTime(System.currentTimeMillis())
//                .build();
//
//        return success(result);
//    }
}