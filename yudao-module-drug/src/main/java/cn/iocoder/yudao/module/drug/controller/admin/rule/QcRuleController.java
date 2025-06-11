package cn.iocoder.yudao.module.drug.controller.admin.rule;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.drug.controller.admin.rule.vo.QcRulePageReqVO;
import cn.iocoder.yudao.module.drug.controller.admin.rule.vo.QcRuleRespVO;
import cn.iocoder.yudao.module.drug.controller.admin.rule.vo.QcRuleSaveReqVO;
import cn.iocoder.yudao.module.drug.dal.dataobject.rule.QcRuleDO;
import cn.iocoder.yudao.module.drug.service.rule.QcRuleService;
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

@Tag(name = "管理后台 - 质控规则")
@RestController
@RequestMapping("/drug/qc-rule")
@Validated
public class QcRuleController {

    @Resource
    private QcRuleService qcRuleService;

    @PostMapping("/create")
    @Operation(summary = "创建质控规则")
    @PreAuthorize("@ss.hasPermission('drug:qc-rule:create')")
    public CommonResult<Long> createQcRule(@Valid @RequestBody QcRuleSaveReqVO createReqVO) {
        return success(qcRuleService.createQcRule(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新质控规则")
    @PreAuthorize("@ss.hasPermission('drug:qc-rule:update')")
    public CommonResult<Boolean> updateQcRule(@Valid @RequestBody QcRuleSaveReqVO updateReqVO) {
        qcRuleService.updateQcRule(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除质控规则")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('drug:qc-rule:delete')")
    public CommonResult<Boolean> deleteQcRule(@RequestParam("id") Long id) {
        qcRuleService.deleteQcRule(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除质控规则")
                @PreAuthorize("@ss.hasPermission('drug:qc-rule:delete')")
    public CommonResult<Boolean> deleteQcRuleList(@RequestParam("ids") List<Long> ids) {
        qcRuleService.deleteQcRuleListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得质控规则")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('drug:qc-rule:query')")
    public CommonResult<QcRuleRespVO> getQcRule(@RequestParam("id") Long id) {
        QcRuleDO qcRule = qcRuleService.getQcRule(id);
        return success(BeanUtils.toBean(qcRule, QcRuleRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得质控规则分页")
    @PreAuthorize("@ss.hasPermission('drug:qc-rule:query')")
    public CommonResult<PageResult<QcRuleRespVO>> getQcRulePage(@Valid QcRulePageReqVO pageReqVO) {
        PageResult<QcRuleDO> pageResult = qcRuleService.getQcRulePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, QcRuleRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出质控规则 Excel")
    @PreAuthorize("@ss.hasPermission('drug:qc-rule:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportQcRuleExcel(@Valid QcRulePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<QcRuleDO> list = qcRuleService.getQcRulePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "质控规则.xls", "数据", QcRuleRespVO.class,
                        BeanUtils.toBean(list, QcRuleRespVO.class));
    }

}