package cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 医疗机构资源信息新增/修改 Request VO")
@Data
public class HosResourceInfoSaveReqVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "17308")
    private Long id;

    @Schema(description = "系统编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "系统编码不能为空")
    private String domainCode;

    @Schema(description = "省级行政区划代码")
    private String provinceCode;

    @Schema(description = "年度药品总收入（元）")
    private BigDecimal annualDrugIncome;

    @Schema(description = "组织机构代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "组织机构代码不能为空")
    private String organizationCode;

    @Schema(description = "组织机构名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotEmpty(message = "组织机构名称不能为空")
    private String organizationName;

    @Schema(description = "医疗机构代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "医疗机构代码不能为空")
    private String hospitalCode;

    @Schema(description = "统计日期(yyyyMMdd)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "统计日期(yyyyMMdd)不能为空")
    private String statDate;

    @Schema(description = "实有床位数")
    private Integer bedsNum;

    @Schema(description = "执业医师数")
    private Integer pracDockerNum;

    @Schema(description = "执业助理医师数")
    private Integer assDockerNum;

    @Schema(description = "总诊疗人次数", example = "13675")
    private Integer visitCount;

    @Schema(description = "出院人数", example = "9451")
    private Integer leaveHosCount;

    @Schema(description = "本季度药品总收入(元)")
    private BigDecimal drugSellAmount;

    @Schema(description = "本季度中药饮片总采购额")
    private BigDecimal ypPurchaseAmount;

    @Schema(description = "本季度中药饮片总销售额")
    private BigDecimal ypSellAmount;

    @Schema(description = "本季度中药颗粒剂总采购额")
    private BigDecimal klPurchaseAmount;

    @Schema(description = "本季度中药颗粒剂总销售额")
    private BigDecimal klSellAmount;

    @Schema(description = "数据上报日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "数据上报日期不能为空")
    private String uploadDate;

    @Schema(description = "导入批次号")
    private String importBatchNo;

}