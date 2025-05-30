package cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo;

import cn.iocoder.yudao.module.dataqc.util.ExcelDataConverter;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 药品目录 Response VO")
@Data
@ExcelIgnoreUnannotated
@Accessors(chain = false)
public class DrugListRespVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "8547")
    private Long id;

    @Schema(description = "数据上报日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("数据上报日期")
    private String uploadDate;

    @Schema(description = "省级行政区划代码") //+
    @ExcelProperty(value = "省级行政区划代码")
    private String provinceCode;

    @Schema(description = "组织机构代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("组织机构代码")
    private String organizationCode;

    @Schema(description = "医疗机构代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("医疗机构代码")
    private String hospitalCode;
    // 医疗机构名称 +
    @Schema(description = "医疗机构名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("医疗机构名称")
    private String hospitalName;

    @Schema(description = "国家药管平台药品编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "21630")
    @ExcelProperty("国家药品编码（YPID）")
    private String ypid;

    @Schema(description = "院内药品唯一码", requiredMode = Schema.RequiredMode.REQUIRED, example = "14027")
    @ExcelProperty("院内药品唯一码")
    private String hosDrugId;

    @Schema(description = "省级药品集中采购平台药品编码", example = "21725")
    @ExcelProperty("省级药品集中采购平台药品编码")
    private String prDrugId;

    @Schema(description = "品种通用名", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @ExcelProperty("通用名")
    private String drugName;
    @Schema(description = "产品名", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @ExcelProperty("产品名称")
    private String productName;

    @Schema(description = "商品名", example = "赵六")
    @ExcelProperty("商品名")
    private String tradeName;

    @Schema(description = "商品名(英文)", example = "王五")
    @ExcelProperty("商品名（英文）")
    private String tradeEngName;

    @Schema(description = "批准文号")
    @ExcelProperty("批准文号")
    private String approvalNum;

    @Schema(description = "生产企业")
    @ExcelProperty("生产企业")
    private String manufacturer;

    @Schema(description = "剂型名称")
    @ExcelProperty("剂型名称")
    private String drugForm;

    @Schema(description = "制剂规格")
    @ExcelProperty("制剂规格")
    private String drugSpec;

    @Schema(description = "制剂单位", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("制剂单位")
    private String dosageUnit;

    @Schema(description = "最小销售包装单位", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("最小销售包装单位")
    private String packUnit;

    @Schema(description = "转换系数", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "转换系数", converter = ExcelDataConverter.SafeBigDecimalConverter.class)
    private BigDecimal drugFactor;

    @Schema(description = "是否基本药物(1是2否)", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("是否基本药物")
    private String baseFlag;

    @Schema(description = "是否网上集中采购药品(1是2否)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String unityPurchaseFlag;

    @Schema(description = "流水号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long serialNum;

    @Schema(description = "系统编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String domainCode;

    @Schema(description = "组织机构名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    private String organizationName;

    @Schema(description = "是否通过一致性评价(1是2否)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String uniformityFlag;

    @Schema(description = "导入批次号")
    private String importBatchNo;

    @Schema(description = "导入时间")
    private LocalDateTime importTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}