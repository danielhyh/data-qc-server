package cn.iocoder.yudao.module.dataqc.controller.admin.drug;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.DrugInoutInfoPageReqVO;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.DrugInoutInfoRespVO;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.DrugInoutInfoSaveReqVO;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.InoutStatVO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.drug.DrugInoutInfoDO;
import cn.iocoder.yudao.module.dataqc.service.drug.DrugInoutInfoService;
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
import java.util.Map;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 药品出入库管理控制器
 * 
 * 功能特色：
 * 1. 统一的出入库数据管理 - 通过ioType区分入库和出库
 * 2. 库存汇总计算 - 实时计算各药品的库存情况  
 * 3. 导入导出支持 - 支持Excel批量导入导出
 * 4. 统计分析功能 - 多维度的出入库数据分析
 * 5. 供应商管理 - 关联供应商信息管理
 */
@Tag(name = "管理后台 - 药品出入库管理")
@RestController
@RequestMapping("/dataqc/drug-inout")
@Validated
@Slf4j
public class DrugInoutController {

    @Resource
    private DrugInoutInfoService drugInoutInfoService;

    /**
     * 创建药品出入库记录
     * 
     * 业务场景：
     * - 手工录入出入库数据
     * - 系统间数据同步
     * - 历史数据补录
     */
    @PostMapping("/create")
    @Operation(summary = "创建药品出入库记录")
    @PreAuthorize("@ss.hasPermission('dataqc:drug-inout:create')")
    public CommonResult<Long> createDrugInoutInfo(@Valid @RequestBody DrugInoutInfoSaveReqVO createReqVO) {
        return success(drugInoutInfoService.createDrugInoutInfo(createReqVO));
    }

    /**
     * 更新药品出入库记录
     * 
     * 应用场景：
     * - 数据纠错和调整
     * - 补充完善信息
     * - 状态变更处理
     */
    @PutMapping("/update")
    @Operation(summary = "更新药品出入库记录")
    @PreAuthorize("@ss.hasPermission('dataqc:drug-inout:update')")
    public CommonResult<Boolean> updateDrugInoutInfo(@Valid @RequestBody DrugInoutInfoSaveReqVO updateReqVO) {
        drugInoutInfoService.updateDrugInoutInfo(updateReqVO);
        return success(true);
    }

    /**
     * 删除药品出入库记录
     * 
     * 重要提醒：
     * - 删除操作会影响库存计算
     * - 建议只删除错误录入的数据
     * - 正常业务数据建议使用状态标记
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除药品出入库记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('dataqc:drug-inout:delete')")
    public CommonResult<Boolean> deleteDrugInoutInfo(@RequestParam("id") Long id) {
        drugInoutInfoService.deleteDrugInoutInfo(id);
        return success(true);
    }

    /**
     * 获得药品出入库记录详情
     */
    @GetMapping("/get")
    @Operation(summary = "获得药品出入库记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('dataqc:drug-inout:query')")
    public CommonResult<DrugInoutInfoRespVO> getDrugInoutInfo(@RequestParam("id") Long id) {
        DrugInoutInfoDO drugInoutInfo = drugInoutInfoService.getDrugInoutInfo(id);
        return success(BeanUtils.toBean(drugInoutInfo, DrugInoutInfoRespVO.class));
    }

    /**
     * 获得药品出入库分页列表
     * 
     * 查询特色：
     * - 支持多条件组合查询
     * - 支持时间范围筛选
     * - 支持药品名称模糊搜索
     * - 支持供应商筛选
     */
    @GetMapping("/page")
    @Operation(summary = "获得药品出入库分页列表")
    @PreAuthorize("@ss.hasPermission('dataqc:drug-inout:query')")
    public CommonResult<PageResult<DrugInoutInfoRespVO>> getDrugInoutInfoPage(@Valid DrugInoutInfoPageReqVO pageReqVO) {
        PageResult<DrugInoutInfoDO> pageResult = drugInoutInfoService.getDrugInoutInfoPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, DrugInoutInfoRespVO.class));
    }

    /**
     * 导出药品出入库数据
     * 
     * 导出特色：
     * - 支持条件筛选导出
     * - 自动格式化数据显示
     * - 包含完整的业务字段
     */
    @GetMapping("/export")
    @Operation(summary = "导出药品出入库数据")
    @PreAuthorize("@ss.hasPermission('dataqc:drug-inout:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportDrugInoutInfo(HttpServletResponse response, @Valid DrugInoutInfoPageReqVO exportReqVO) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<DrugInoutInfoDO> list = drugInoutInfoService.selectInoutList(exportReqVO);
        ExcelUtils.write(response, "药品出入库数据.xls", "数据", DrugInoutInfoRespVO.class,
                BeanUtils.toBean(list, DrugInoutInfoRespVO.class));
    }

    /**
     * 导入药品入库数据
     * 
     * 导入流程：
     * 1. 文件格式校验
     * 2. 数据内容校验  
     * 3. 药品目录关联校验
     * 4. 批量数据保存
     * 5. 返回导入结果统计
     */
    @PostMapping("/import-in")
    @Operation(summary = "导入药品入库数据")
    @PreAuthorize("@ss.hasPermission('dataqc:drug-inout:import')")
    public CommonResult<String> importInData(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) throws Exception {
        String result = drugInoutInfoService.importInData(file, updateSupport);
        return success(result);
    }

    /**
     * 导入药品出库数据
     */
    @PostMapping("/import-out")
    @Operation(summary = "导入药品出库数据") 
    @PreAuthorize("@ss.hasPermission('dataqc:drug-inout:import')")
    public CommonResult<String> importOutData(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) throws Exception {
        String result = drugInoutInfoService.importOutData(file, updateSupport);
        return success(result);
    }

    /**
     * 获取库存汇总
     * 
     * 计算逻辑：
     * - 按药品编码汇总入库和出库数量
     * - 计算当前库存 = 入库总量 - 出库总量
     * - 显示最新入库价格和库存金额
     * - 只显示有库存的药品
     */
    @GetMapping("/stock-summary")
    @Operation(summary = "获取库存汇总")
    @PreAuthorize("@ss.hasPermission('dataqc:drug-inout:query')")
    public CommonResult<List<Map<String, Object>>> getStockSummary(@Valid DrugInoutInfoPageReqVO queryVO) {
        List<Map<String, Object>> result = drugInoutInfoService.getStockSummary(queryVO);
        return success(result);
    }

    /**
     * 获取出入库统计
     * 
     * 统计维度：
     * - 入库统计：次数、数量、金额
     * - 出库统计：次数、数量  
     * - 供应商统计：供应商数量
     * - 库存分析：余量、周转率等
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取出入库统计")
    @PreAuthorize("@ss.hasPermission('dataqc:drug-inout:query')")
    public CommonResult<InoutStatVO> getInoutStatistics(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        InoutStatVO result = drugInoutInfoService.getInoutStatistics(startDate, endDate);
        return success(result);
    }

    /**
     * 获取即将过期预警
     * 
     * 预警机制：
     * - 根据有效期计算剩余天数
     * - 筛选指定天数内过期的药品
     * - 显示库存数量和批次信息
     * - 按过期时间排序便于处理
     */
    @GetMapping("/expiry-warning")
    @Operation(summary = "获取即将过期预警")
    @PreAuthorize("@ss.hasPermission('dataqc:drug-inout:query')")
    public CommonResult<List<Map<String, Object>>> getExpiryWarning(
            @RequestParam(value = "days", defaultValue = "30") Integer days) {
        // 这里需要在Service中实现对应方法
        // List<Map<String, Object>> result = drugInoutInfoService.getExpiryWarning(days);
        // return success(result);
        return success(List.of()); // 临时返回空列表
    }

    /**
     * 获取供应商统计
     * 
     * 统计内容：
     * - 供应商采购药品种类数
     * - 采购总数量和总金额  
     * - 采购订单数量
     * - 按采购金额排序
     */
    @GetMapping("/supplier-stats")
    @Operation(summary = "获取供应商统计")
    @PreAuthorize("@ss.hasPermission('dataqc:drug-inout:query')")
    public CommonResult<List<Map<String, Object>>> getSupplierStats(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        // 这里需要在Service中实现对应方法
        // List<Map<String, Object>> result = drugInoutInfoService.getSupplierStats(startDate, endDate);
        // return success(result);
        return success(List.of()); // 临时返回空列表
    }
}