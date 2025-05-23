package cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 药品使用情况新增/修改 Request VO")
@Data
public class DrugUseInfoSaveReqVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "19890")
    private Long id;

    @Schema(description = "流水号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "流水号不能为空")
    private Long serialNum;

    @Schema(description = "系统编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "系统编码不能为空")
    private String domainCode;

    @Schema(description = "组织机构代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "组织机构代码不能为空")
    private String organizationCode;

    @Schema(description = "组织机构名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotEmpty(message = "组织机构名称不能为空")
    private String organizationName;

    @Schema(description = "医疗机构代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "医疗机构代码不能为空")
    private String hospitalCode;

    @Schema(description = "数据上报日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "数据上报日期不能为空")
    private String uploadDate;

    @Schema(description = "国家药管平台药品编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "4317")
    @NotEmpty(message = "国家药管平台药品编码不能为空")
    private String ypid;

    @Schema(description = "省级药品集中采购平台药品编码", example = "14829")
    private String prDrugId;

    @Schema(description = "院内药品唯一码", requiredMode = Schema.RequiredMode.REQUIRED, example = "29079")
    @NotEmpty(message = "院内药品唯一码不能为空")
    private String hosDrugId;

    @Schema(description = "产品通用名", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @NotEmpty(message = "产品通用名不能为空")
    private String productName;

    @Schema(description = "药品销售日期(yyyyMMdd)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "药品销售日期(yyyyMMdd)不能为空")
    private String sellDate;

    @Schema(description = "销售价格(最小销售包装单位)", requiredMode = Schema.RequiredMode.REQUIRED, example = "5596")
    @NotNull(message = "销售价格(最小销售包装单位)不能为空")
    private BigDecimal sellPackPrice;

    @Schema(description = "销售数量(最小销售包装单位)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "销售数量(最小销售包装单位)不能为空")
    private Long sellPackQuantity;

    @Schema(description = "销售价格(最小制剂单位)", requiredMode = Schema.RequiredMode.REQUIRED, example = "20773")
    @NotNull(message = "销售价格(最小制剂单位)不能为空")
    private BigDecimal sellDosagePrice;

    @Schema(description = "销售数量(最小制剂单位)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "销售数量(最小制剂单位)不能为空")
    private Long sellDosageQuantity;

    @Schema(description = "使用科室代码")
    private String departmentCode;

    @Schema(description = "使用科室名称", example = "王五")
    private String departmentName;

    @Schema(description = "开具医生代码")
    private String doctorCode;

    @Schema(description = "开具医生姓名", example = "王五")
    private String doctorName;

    @Schema(description = "患者类型(门诊/住院)", example = "2")
    private String patientType;

    @Schema(description = "导入批次号")
    private String importBatchNo;

    @Schema(description = "导入时间")
    private LocalDateTime importTime;

}