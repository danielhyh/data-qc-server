package cn.iocoder.yudao.module.drug.controller.admin.batch;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.drug.controller.admin.batch.vo.ImportTaskPageReqVO;
import cn.iocoder.yudao.module.drug.controller.admin.batch.vo.ImportTaskRespVO;
import cn.iocoder.yudao.module.drug.controller.admin.batch.vo.ImportTaskSaveReqVO;
import cn.iocoder.yudao.module.drug.dal.dataobject.batch.ImportTaskDO;
import cn.iocoder.yudao.module.drug.service.batch.ImportTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 药品数据导入任务")
@RestController
@RequestMapping("/drug/import-task")
@Validated
public class ImportTaskController {

    @Resource
    private ImportTaskService importTaskService;

    @PostMapping("/create")
    @Operation(summary = "创建药品数据导入任务")
    @PreAuthorize("@ss.hasPermission('drug:import-task:create')")
    public CommonResult<Long> createImportTask(@Valid @RequestBody ImportTaskSaveReqVO createReqVO) {
        return success(importTaskService.createImportTask(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新药品数据导入任务")
    @PreAuthorize("@ss.hasPermission('drug:import-task:update')")
    public CommonResult<Boolean> updateImportTask(@Valid @RequestBody ImportTaskSaveReqVO updateReqVO) {
        importTaskService.updateImportTask(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除药品数据导入任务")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('drug:import-task:delete')")
    public CommonResult<Boolean> deleteImportTask(@RequestParam("id") Long id) {
        importTaskService.deleteImportTask(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除药品数据导入任务")
                @PreAuthorize("@ss.hasPermission('drug:import-task:delete')")
    public CommonResult<Boolean> deleteImportTaskList(@RequestParam("ids") List<Long> ids) {
        importTaskService.deleteImportTaskListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得药品数据导入任务")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('drug:import-task:query')")
    public CommonResult<ImportTaskRespVO> getImportTask(@RequestParam("id") Long id) {
        ImportTaskDO importTask = importTaskService.getImportTask(id);
        return success(BeanUtils.toBean(importTask, ImportTaskRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得药品数据导入任务分页")
    @PreAuthorize("@ss.hasPermission('drug:import-task:query')")
    public CommonResult<PageResult<ImportTaskRespVO>> getImportTaskPage(@Valid ImportTaskPageReqVO pageReqVO) {
        PageResult<ImportTaskDO> pageResult = importTaskService.getImportTaskPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ImportTaskRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出药品数据导入任务 Excel")
    @PreAuthorize("@ss.hasPermission('drug:import-task:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportImportTaskExcel(@Valid ImportTaskPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ImportTaskDO> list = importTaskService.getImportTaskPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "药品数据导入任务.xls", "数据", ImportTaskRespVO.class,
                        BeanUtils.toBean(list, ImportTaskRespVO.class));
    }

}