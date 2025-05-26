package cn.iocoder.yudao.module.dataqc.controller.admin.drug;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.DrugUseInfoPageReqVO;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.DrugUseInfoSaveReqVO;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.DrugUseInfoRespVO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.drug.DrugUseInfoDO;
import cn.iocoder.yudao.module.dataqc.service.drug.DrugUseInfoService;
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
 * 药品使用情况管理控制器
 * 
 * 核心业务价值：
 * 1. 用药行为分析 - 追踪医生开药模式和患者用药习惯
 * 2. 基药使用监管 - 确保基本药物使用比例符合政策要求
 * 3. 科室用药评估 - 分析各科室用药结构和费用控制情况
 * 4. 合理用药控制 - 识别异常用药行为，支持临床决策
 * 5. 费用统计分析 - 提供详细的药品费用构成和趋势分析
 */
@Tag(name = "管理后台 - 药品使用情况管理")
@RestController
@RequestMapping("/dataqc/drug-use")
@Validated
@Slf4j
public class DrugUseController {

    @Resource
    private DrugUseInfoService drugUseInfoService;

    /**
     * 创建药品使用记录
     * 
     * 应用场景：
     * - 处方信息录入
     * - 用药记录补录
     * - 系统数据同步
     * - 临床试验记录
     */
    @PostMapping("/create")
    @Operation(summary = "创建药品使用记录")
    @PreAuthorize("@ss.hasPermission('dataqc:drug-use:create')")
    public CommonResult<Long> createDrugUseInfo(@Valid @RequestBody DrugUseInfoSaveReqVO createReqVO) {
        return success(drugUseInfoService.createDrugUseInfo(createReqVO));
    }

    /**
     * 更新药品使用记录
     * 
     * 业务需求：
     * - 处方信息修正
     * - 价格调整处理
     * - 科室信息变更
     * - 患者类型更新
     */
    @PutMapping("/update")
    @Operation(summary = "更新药品使用记录")
    @PreAuthorize("@ss.hasPermission('dataqc:drug-use:update')")
    public CommonResult<Boolean> updateDrugUseInfo(@Valid @RequestBody DrugUseInfoSaveReqVO updateReqVO) {
        drugUseInfoService.updateDrugUseInfo(updateReqVO);
        return success(true);
    }

    /**
     * 删除药品使用记录
     * 
     * 注意事项：
     * - 删除操作需要详细审计日志
     * - 涉及费用统计的数据删除需要特别谨慎
     * - 建议采用逻辑删除而非物理删除
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除药品使用记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('dataqc:drug-use:delete')")
    public CommonResult<Boolean> deleteDrugUseInfo(@RequestParam("id") Long id) {
        drugUseInfoService.deleteDrugUseInfo(id);
        return success(true);
    }

    /**
     * 获得药品使用记录详情
     */
    @GetMapping("/get")
    @Operation(summary = "获得药品使用记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('dataqc:drug-use:query')")
    public CommonResult<DrugUseInfoRespVO> getDrugUseInfo(@RequestParam("id") Long id) {
        DrugUseInfoDO drugUseInfo = drugUseInfoService.getDrugUseInfo(id);
        return success(BeanUtils.toBean(drugUseInfo, DrugUseInfoRespVO.class));
    }

    /**
     * 获得药品使用情况分页列表
     * 
     * 查询功能特色：
     * - 支持按科室、医生、患者类型等多维度筛选
     * - 提供时间范围查询，支持用药趋势分析
     * - 集成药品信息，显示完整的用药上下文
     * - 支持模糊搜索，提升查询效率
     */
    @GetMapping("/page")
    @Operation(summary = "获得药品使用情况分页列表")
    @PreAuthorize("@ss.hasPermission('dataqc:drug-use:query')")
    public CommonResult<PageResult<DrugUseInfoRespVO>> getDrugUseInfoPage(@Valid DrugUseInfoPageReqVO pageReqVO) {
        PageResult<DrugUseInfoDO> pageResult = drugUseInfoService.getDrugUseInfoPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, DrugUseInfoRespVO.class));
    }

    /**
     * 导出药品使用数据
     * 
     * 导出价值：
     * - 支持监管部门检查和审计
     * - 便于进行深度数据分析
     * - 提供历史数据备份
     * - 支持跨系统数据交换
     */
    @GetMapping("/export")
    @Operation(summary = "导出药品使用数据")
    @PreAuthorize("@ss.hasPermission('dataqc:drug-use:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportDrugUseInfo(HttpServletResponse response, @Valid DrugUseInfoPageReqVO exportReqVO) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<DrugUseInfoDO> list = drugUseInfoService.selectUseList(exportReqVO);
        ExcelUtils.write(response, "药品使用数据.xls", "数据", DrugUseInfoRespVO.class,
                BeanUtils.toBean(list, DrugUseInfoRespVO.class));
    }

    /**
     * 导入药品使用数据
     * 
     * 导入优势：
     * - 批量处理大量处方数据
     * - 自动关联药品目录信息
     * - 智能数据校验和错误提示
     * - 支持增量更新和全量替换
     */
    @PostMapping("/import")
    @Operation(summary = "导入药品使用数据")
    @PreAuthorize("@ss.hasPermission('dataqc:drug-use:import')")
    public CommonResult<String> importUseData(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) throws Exception {
        String result = drugUseInfoService.importUseData(file, updateSupport);
        return success(result);
    }

    /**
     * 获取用药统计分析
     * 
     * 统计维度包括：
     * - 总体用药规模：处方数量、用药金额、涉及药品种类
     * - 科室分布：各科室用药结构和费用占比
     * - 患者类型：门诊、急诊、住院患者用药对比
     * - 时间趋势：用药量和费用的季节性变化
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取用药统计分析")
    @PreAuthorize("@ss.hasPermission('dataqc:drug-use:query')")
    public CommonResult<List<Map<String, Object>>> getUseStatistics(@Valid DrugUseInfoPageReqVO queryVO) {
        List<Map<String, Object>> result = drugUseInfoService.getUseStatistics(queryVO);
        return success(result);
    }

    /**
     * 获取科室用药排名
     * 
     * 排名价值：
     * - 识别高用药科室，重点关注费用控制
     * - 分析科室间用药差异，优化资源配置
     * - 计算基药使用率，确保政策合规
     * - 提供科室绩效评估的数据支撑
     */
    @GetMapping("/department-ranking")
    @Operation(summary = "获取科室用药排名")
    @PreAuthorize("@ss.hasPermission('dataqc:drug-use:query')")
    public CommonResult<List<Map<String, Object>>> getDepartmentRanking(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        List<Map<String, Object>> result = drugUseInfoService.getDepartmentRanking(startDate, endDate);
        return success(result);
    }

    /**
     * 获取药品使用排名
     * 
     * 排名作用：
     * - 识别高频使用药品，指导采购计划
     * - 分析用药结构，优化药品配置
     * - 监控昂贵药品使用，控制医疗成本
     * - 发现用药异常，支持临床决策
     */
    @GetMapping("/drug-ranking")
    @Operation(summary = "获取药品使用排名")
    @PreAuthorize("@ss.hasPermission('dataqc:drug-use:query')")
    public CommonResult<List<Map<String, Object>>> getDrugUseRanking(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        List<Map<String, Object>> result = drugUseInfoService.getDrugUseRanking(startDate, endDate);
        return success(result);
    }

    /**
     * 获取基药使用分析
     * 
     * 分析重点：
     * - 基药使用率计算和趋势分析
     * - 基药与非基药的费用对比
     * - 基药政策执行情况评估
     * - 科室基药使用合规性检查
     */
    @GetMapping("/base-drug-analysis")
    @Operation(summary = "获取基药使用分析")
    @PreAuthorize("@ss.hasPermission('dataqc:drug-use:query')")
    public CommonResult<Map<String, Object>> getBaseDrugAnalysis(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        // 这里需要在Service中实现对应方法
        // Map<String, Object> result = drugUseInfoService.getBaseDrugAnalysis(startDate, endDate);
        // return success(result);
        return success(Map.of()); // 临时返回空Map
    }

    /**
     * 获取医生处方分析
     * 
     * 分析目标：
     * - 识别高开药医生，关注处方合理性
     * - 分析医生用药偏好，提供临床指导
     * - 监控处方金额异常，防范违规行为
     * - 支持医生绩效评估和培训需求分析
     */
    @GetMapping("/doctor-analysis")
    @Operation(summary = "获取医生处方分析")
    @PreAuthorize("@ss.hasPermission('dataqc:drug-use:query')")
    public CommonResult<List<Map<String, Object>>> getDoctorAnalysis(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam(value = "limit", defaultValue = "20") Integer limit) {
        // 这里需要在Service中实现对应方法
        // List<Map<String, Object>> result = drugUseInfoService.getDoctorAnalysis(startDate, endDate, limit);
        // return success(result);
        return success(List.of()); // 临时返回空列表
    }

    /**
     * 获取患者类型用药分析
     * 
     * 分析维度：
     * - 门诊、急诊、住院患者用药结构对比
     * - 不同患者类型的平均用药费用
     * - 各类型患者的常用药品分析
     * - 用药安全风险评估
     */
    @GetMapping("/patient-type-analysis")
    @Operation(summary = "获取患者类型用药分析")
    @PreAuthorize("@ss.hasPermission('dataqc:drug-use:query')")
    public CommonResult<List<Map<String, Object>>> getPatientTypeAnalysis(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        // 这里需要在Service中实现对应方法
        // List<Map<String, Object>> result = drugUseInfoService.getPatientTypeAnalysis(startDate, endDate);
        // return success(result);
        return success(List.of()); // 临时返回空列表
    }
}