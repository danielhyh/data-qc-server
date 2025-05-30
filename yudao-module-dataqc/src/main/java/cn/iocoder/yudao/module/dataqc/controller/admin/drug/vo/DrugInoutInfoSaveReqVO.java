package cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 药品出入库新增/修改 Request VO")
@Data
public class DrugInoutInfoSaveReqVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10445")
    private Long id;

    @Schema(description = "流水号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "流水号不能为空")
    private Long serialNum;

    @Schema(description = "系统编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "系统编码不能为空")
    private String domainCode;

    @Schema(description = "省级行政区划代码") //+
    private String provinceCode;

    @Schema(description = "入库总金额(元)", requiredMode = Schema.RequiredMode.REQUIRED, example = "26800")
    private BigDecimal inTotalPrice;

    @Schema(description = "组织机构代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "组织机构代码不能为空")
    private String organizationCode;

    @Schema(description = "组织机构名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @NotEmpty(message = "组织机构名称不能为空")
    private String organizationName;

    @Schema(description = "医疗机构代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "医疗机构代码不能为空")
    private String hospitalCode;

    @Schema(description = "数据上报日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "数据上报日期不能为空")
    private String uploadDate;

    @Schema(description = "国家药管平台药品编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "27389")
    @NotEmpty(message = "国家药管平台药品编码不能为空")
    private String ypid;

    @Schema(description = "省级药品集中采购平台药品编码", example = "5070")
    private String prDrugId;

    @Schema(description = "院内药品唯一码", requiredMode = Schema.RequiredMode.REQUIRED, example = "12699")
    @NotEmpty(message = "院内药品唯一码不能为空")
    private String hosDrugId;

    @Schema(description = "产品通用名", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @NotEmpty(message = "产品通用名不能为空")
    private String productName;

    @Schema(description = "出入库时间(yyyyMMdd)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "出入库时间(yyyyMMdd)不能为空")
    private String outInDate;

    @Schema(description = "出入库类型(IN-入库,OUT-出库)", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "出入库类型(IN-入库,OUT-出库)不能为空")
    private String ioType;

    @Schema(description = "入库数量(最小销售包装单位)")
    private Long inPackQuantity;

    @Schema(description = "入库数量(最小制剂单位)")
    private Long inDosageQuantity;

    @Schema(description = "入库价格(最小销售包装单位)", example = "31445")
    private BigDecimal inPackPrice;

    @Schema(description = "入库价格(最小制剂单位)", example = "26800")
    private BigDecimal inDosagePrice;

    @Schema(description = "出库数量(最小销售包装单位)")
    private Long outPackQuantity;

    @Schema(description = "出库数量(最小制剂单位)")
    private Long outDosageQuantity;

    @Schema(description = "供应商代码")
    private String supplierCode;

    @Schema(description = "供应商名称", example = "赵六")
    private String supplierName;

    @Schema(description = "批号")
    private String batchNo;

    @Schema(description = "生产日期")
    private String productionDate;

    @Schema(description = "有效期至")
    private String expiryDate;

    @Schema(description = "导入批次号")
    private String importBatchNo;

    @Schema(description = "导入时间")
    private LocalDateTime importTime;

    @Schema(description = "数据来源(IN_IMPORT-入库导入,OUT_IMPORT-出库导入)", example = "2")
    private String sourceType;

}