package cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 药品目录新增/修改 Request VO")
@Data
public class DrugListSaveReqVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "8547")
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

    @Schema(description = "国家药管平台药品编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "21630")
    @NotEmpty(message = "国家药管平台药品编码不能为空")
    private String ypid;

    @Schema(description = "省级药品集中采购平台药品编码", example = "21725")
    private String prDrugId;

    @Schema(description = "院内药品唯一码", requiredMode = Schema.RequiredMode.REQUIRED, example = "14027")
    @NotEmpty(message = "院内药品唯一码不能为空")
    private String hosDrugId;

    @Schema(description = "批准文号")
    private String approvalNum;

    @Schema(description = "品种通用名", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotEmpty(message = "品种通用名不能为空")
    private String drugName;

    @Schema(description = "产品通用名", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @NotEmpty(message = "产品通用名不能为空")
    private String productName;

    @Schema(description = "商品名", example = "赵六")
    private String tradeName;

    @Schema(description = "商品名(英文)", example = "王五")
    private String tradeEngName;

    @Schema(description = "生产企业")
    private String manufacturer;

    @Schema(description = "剂型名称")
    private String drugForm;

    @Schema(description = "制剂规格")
    private String drugSpec;

    @Schema(description = "制剂单位", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "制剂单位不能为空")
    private String dosageUnit;

    @Schema(description = "最小销售包装单位", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "最小销售包装单位不能为空")
    private String packUnit;

    @Schema(description = "转换系数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "转换系数不能为空")
    private BigDecimal drugFactor;

    @Schema(description = "是否网上集中采购药品(1是2否)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "是否网上集中采购药品(1是2否)不能为空")
    private String unityPurchaseFlag;

    @Schema(description = "是否基本药物(1是2否)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "是否基本药物(1是2否)不能为空")
    private String baseFlag;

    @Schema(description = "是否通过一致性评价(1是2否)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "是否通过一致性评价(1是2否)不能为空")
    private String uniformityFlag;

    @Schema(description = "导入批次号")
    private String importBatchNo;

    @Schema(description = "导入时间")
    private LocalDateTime importTime;

}