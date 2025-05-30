package cn.iocoder.yudao.module.drug.controller.admin.batch;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.drug.controller.admin.batch.vo.ImportTaskDetailPageReqVO;
import cn.iocoder.yudao.module.drug.controller.admin.batch.vo.ImportTaskDetailRespVO;
import cn.iocoder.yudao.module.drug.controller.admin.batch.vo.ImportTaskDetailSaveReqVO;
import cn.iocoder.yudao.module.drug.dal.dataobject.batch.ImportTaskDetailDO;
import cn.iocoder.yudao.module.drug.service.batch.ImportTaskDetailService;
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

@Tag(name = "管理后台 - 药品数据导入任务明细")
@RestController
@RequestMapping("/drug/import-task-detail")
@Validated
public class ImportTaskDetailController {

    @Resource
    private ImportTaskDetailService importTaskDetailService;

    @PostMapping("/create")
    @Operation(summary = "创建药品数据导入任务明细")
    @PreAuthorize("@ss.hasPermission('drug:import-task-detail:create')")
    public CommonResult<Long> createImportTaskDetail(@Valid @RequestBody ImportTaskDetailSaveReqVO createReqVO) {
        return success(importTaskDetailService.createImportTaskDetail(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新药品数据导入任务明细")
    @PreAuthorize("@ss.hasPermission('drug:import-task-detail:update')")
    public CommonResult<Boolean> updateImportTaskDetail(@Valid @RequestBody ImportTaskDetailSaveReqVO updateReqVO) {
        importTaskDetailService.updateImportTaskDetail(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除药品数据导入任务明细")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('drug:import-task-detail:delete')")
    public CommonResult<Boolean> deleteImportTaskDetail(@RequestParam("id") Long id) {
        importTaskDetailService.deleteImportTaskDetail(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除药品数据导入任务明细")
                @PreAuthorize("@ss.hasPermission('drug:import-task-detail:delete')")
    public CommonResult<Boolean> deleteImportTaskDetailList(@RequestParam("ids") List<Long> ids) {
        importTaskDetailService.deleteImportTaskDetailListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得药品数据导入任务明细")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('drug:import-task-detail:query')")
    public CommonResult<ImportTaskDetailRespVO> getImportTaskDetail(@RequestParam("id") Long id) {
        ImportTaskDetailDO importTaskDetail = importTaskDetailService.getImportTaskDetail(id);
        return success(BeanUtils.toBean(importTaskDetail, ImportTaskDetailRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得药品数据导入任务明细分页")
    @PreAuthorize("@ss.hasPermission('drug:import-task-detail:query')")
    public CommonResult<PageResult<ImportTaskDetailRespVO>> getImportTaskDetailPage(@Valid ImportTaskDetailPageReqVO pageReqVO) {
        PageResult<ImportTaskDetailDO> pageResult = importTaskDetailService.getImportTaskDetailPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ImportTaskDetailRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出药品数据导入任务明细 Excel")
    @PreAuthorize("@ss.hasPermission('drug:import-task-detail:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportImportTaskDetailExcel(@Valid ImportTaskDetailPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ImportTaskDetailDO> list = importTaskDetailService.getImportTaskDetailPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "药品数据导入任务明细.xls", "数据", ImportTaskDetailRespVO.class,
                        BeanUtils.toBean(list, ImportTaskDetailRespVO.class));
    }

}