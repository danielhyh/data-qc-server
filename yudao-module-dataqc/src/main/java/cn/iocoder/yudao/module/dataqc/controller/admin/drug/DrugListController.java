package cn.iocoder.yudao.module.dataqc.controller.admin.drug;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.DrugListPageReqVO;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.DrugListRespVO;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.DrugListSaveReqVO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.drug.DrugListDO;
import cn.iocoder.yudao.module.dataqc.service.drug.DrugListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 药品目录管理控制器
 * 
 * 功能特色：
 * 1. 统一的药品目录数据管理 - 支持医院药品目录维护
 * 2. 标准化映射管理 - 院内编码与统一编码关联
 * 3. 导入导出支持 - 支持Excel批量导入导出
 * 4. 重复性校验 - 防止重复药品录入
 * 5. 基药目录标识 - 支持基本药物目录管理
 * 6. 集采标识管理 - 统一采购药品标记
 */
@Tag(name = "管理后台 - 药品目录管理")
@RestController
@RequestMapping("/dataqc/drug-list")
@Validated
@Slf4j
public class DrugListController {

    @Resource
    private DrugListService drugListService;

    /**
     * 创建药品目录记录
     * 
     * 业务场景：
     * - 新药品上架录入
     * - 药品信息标准化录入
     * - 系统间数据同步
     */
    @PostMapping("/create")
    @Operation(summary = "创建药品目录记录")
    @PreAuthorize("@ss.hasPermission('dataqc:drug-list:create')")
    public CommonResult<Long> createDrugList(@Valid @RequestBody DrugListSaveReqVO createReqVO) {
        return success(drugListService.createDrugList(createReqVO));
    }

    /**
     * 更新药品目录记录
     * 
     * 应用场景：
     * - 药品信息变更维护
     * - 规格包装调整
     * - 价格信息更新
     * - 状态标识变更
     */
    @PutMapping("/update")
    @Operation(summary = "更新药品目录记录")
    @PreAuthorize("@ss.hasPermission('dataqc:drug-list:update')")
    public CommonResult<Boolean> updateDrugList(@Valid @RequestBody DrugListSaveReqVO updateReqVO) {
        drugListService.updateDrugList(updateReqVO);
        return success(true);
    }

    /**
     * 删除药品目录记录
     * 
     * 重要提醒：
     * - 删除前需检查是否有关联的出入库记录
     * - 建议采用停用状态而非物理删除
     * - 删除操作影响历史数据完整性
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除药品目录记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('dataqc:drug-list:delete')")
    public CommonResult<Boolean> deleteDrugList(@RequestParam("id") Long id) {
        drugListService.deleteDrugList(id);
        return success(true);
    }

    /**
     * 批量删除药品目录记录
     */
    @DeleteMapping("/delete-batch")
    @Operation(summary = "批量删除药品目录记录")
    @PreAuthorize("@ss.hasPermission('dataqc:drug-list:delete')")
    public CommonResult<Boolean> deleteDrugListBatch(@RequestBody List<Long> ids) {
        drugListService.deleteDrugListListByIds(ids);
        return success(true);
    }

    /**
     * 获得药品目录记录详情
     */
    @GetMapping("/get")
    @Operation(summary = "获得药品目录记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('dataqc:drug-list:query')")
    public CommonResult<DrugListRespVO> getDrugList(@RequestParam("id") Long id) {
        DrugListDO drugList = drugListService.getDrugList(id);
        return success(BeanUtils.toBean(drugList, DrugListRespVO.class));
    }

    /**
     * 根据院内药品编码获取药品信息
     * 
     * 用途：
     * - 出入库录入时药品信息联动
     * - 处方开具时药品验证
     * - 第三方系统数据对接
     */
    @GetMapping("/get-by-hos-drug-id")
    @Operation(summary = "根据院内药品编码获取药品信息")
    @Parameter(name = "hosDrugId", description = "院内药品编码", required = true)
    @PreAuthorize("@ss.hasPermission('dataqc:drug-list:query')")
    public CommonResult<DrugListRespVO> getDrugByHosDrugId(@RequestParam("hosDrugId") String hosDrugId) {
        DrugListDO drugList = drugListService.selectByHosDrugId(hosDrugId);
        return success(BeanUtils.toBean(drugList, DrugListRespVO.class));
    }

    /**
     * 检查院内药品编码是否已存在
     * 
     * 用于前端实时校验，避免重复录入
     */
    @GetMapping("/check-hos-drug-id")
    @Operation(summary = "检查院内药品编码是否已存在")
    @PreAuthorize("@ss.hasPermission('dataqc:drug-list:query')")
    public CommonResult<Boolean> checkHosDrugIdExist(
            @RequestParam("hosDrugId") String hosDrugId,
            @RequestParam(value = "excludeId", required = false) Long excludeId) {
        boolean exists = drugListService.checkHosDrugIdExist(hosDrugId, excludeId);
        return success(exists);
    }

    /**
     * 获得药品目录分页列表
     * 
     * 查询特色：
     * - 支持多条件组合查询
     * - 支持药品名称模糊搜索
     * - 支持生产厂家筛选
     * - 支持基药/集采标识筛选
     */
    @GetMapping("/page")
    @Operation(summary = "获得药品目录分页列表")
    @PreAuthorize("@ss.hasPermission('dataqc:drug-list:query')")
    public CommonResult<PageResult<DrugListRespVO>> getDrugListPage(@Valid DrugListPageReqVO pageReqVO) {
        PageResult<DrugListDO> pageResult = drugListService.getDrugListPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, DrugListRespVO.class));
    }

    /**
     * 导出药品目录数据
     * 
     * 导出特色：
     * - 支持条件筛选导出
     * - 包含完整的药品基础信息
     * - 自动格式化规格和包装信息
     */
    @GetMapping("/export")
    @Operation(summary = "导出药品目录数据")
    @PreAuthorize("@ss.hasPermission('dataqc:drug-list:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportDrugList(HttpServletResponse response, @Valid DrugListPageReqVO exportReqVO) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<DrugListDO> list = drugListService.selectDrugList(exportReqVO);
        ExcelUtils.write(response, "药品目录数据.xls", "数据", DrugListRespVO.class,
                BeanUtils.toBean(list, DrugListRespVO.class));
    }

    /**
     * 导入药品目录数据
     * 
     * 导入流程：
     * 1. 文件格式校验
     * 2. 药品基础信息校验
     * 3. 院内编码重复性校验
     * 4. 统一编码关联校验
     * 5. 批量数据保存或更新
     * 6. 返回导入结果统计
     */
    @PostMapping("/import")
    @Operation(summary = "导入药品目录数据")
    @PreAuthorize("@ss.hasPermission('dataqc:drug-list:import')")
    public CommonResult<String> importDrugList(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) throws Exception {
        String result = drugListService.importDrugList(file, updateSupport);
        return success(result);
    }

    /**
     * 获取药品目录模板
     * 
     * 提供标准的导入模板，确保数据格式统一
     */
    @GetMapping("/get-import-template")
    @Operation(summary = "获得药品目录导入模板")
    @PreAuthorize("@ss.hasPermission('dataqc:drug-list:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "药品目录导入模板.xls", "药品目录", DrugListRespVO.class, List.of());
    }

    /**
     * 获取基本药物目录统计
     * 
     * 统计维度：
     * - 基药品种数量
     * - 非基药品种数量
     * - 基药覆盖率
     */
    @GetMapping("/base-drug-stats")
    @Operation(summary = "获取基本药物目录统计")
    @PreAuthorize("@ss.hasPermission('dataqc:drug-list:query')")
    public CommonResult<Object> getBaseDrugStats() {
        // 待Service层实现
        return success("统计功能开发中");
    }

    /**
     * 获取集采药品统计
     * 
     * 统计维度：
     * - 集采药品种类数
     * - 集采覆盖率分析
     * - 各批次集采分布
     */
    @GetMapping("/unity-purchase-stats")
    @Operation(summary = "获取集采药品统计")
    @PreAuthorize("@ss.hasPermission('dataqc:drug-list:query')")
    public CommonResult<Object> getUnityPurchaseStats() {
        // 待Service层实现
        return success("统计功能开发中");
    }

    /**
     * 药品目录一致性检查
     * 
     * 检查内容：
     * - 院内编码与统一编码映射完整性
     * - 药品基础信息完整性
     * - 重复数据检测
     */
    @GetMapping("/consistency-check")
    @Operation(summary = "药品目录一致性检查")
    @PreAuthorize("@ss.hasPermission('dataqc:drug-list:query')")
    public CommonResult<Object> consistencyCheck(@Valid DrugListPageReqVO checkReqVO) {
        // 待Service层实现
        return success("一致性检查功能开发中");
    }

}