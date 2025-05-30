package cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo;

import cn.iocoder.yudao.module.dataqc.util.ExcelDataConverter;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 药品使用情况 Response VO")
@Data
@ExcelIgnoreUnannotated
@Accessors(chain = false)
public class DrugUseInfoRespVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "19890")
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

    @Schema(description = "组织机构名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @ExcelProperty("组织机构名称")
    private String organizationName;

    @Schema(description = "医疗机构代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("医疗机构代码")
    private String hospitalCode;

    @Schema(description = "国家药管平台药品编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "4317")
    @ExcelProperty("国家药品编码（YPID）")
    private String ypid;

    @Schema(description = "省级药品集中采购平台药品编码", example = "14829")
    @ExcelProperty("省级药品集中采购平台药品编码")
    private String prDrugId;

    @Schema(description = "院内药品唯一码", requiredMode = Schema.RequiredMode.REQUIRED, example = "29079")
    @ExcelProperty("院内药品唯一码")
    private String hosDrugId;

    @Schema(description = "产品通用名", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @ExcelProperty("产品名称")
    private String productName;

    // 销售总金额（元）+
    @Schema(description = "销售总金额（元）", requiredMode = Schema.RequiredMode.REQUIRED, example = "13038")
    @ExcelProperty(value = "销售总金额（元）", converter = ExcelDataConverter.SafeBigDecimalConverter.class)
    private BigDecimal sellTotalPrice;

    @Schema(description = "销售数量（最小销售包装单位）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "销售数量（最小销售包装单位）", converter = ExcelDataConverter.SafeLongConverter.class)
    private Long sellPackQuantity;

    @Schema(description = "销售数量(最小制剂单位)", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "销售数量（最小制剂单位）", converter = ExcelDataConverter.SafeLongConverter.class)
    private Long sellDosageQuantity;

    @Schema(description = "流水号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long serialNum;

    @Schema(description = "系统编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String domainCode;

    @Schema(description = "药品销售日期(yyyyMMdd)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sellDate;

    @Schema(description = "销售价格(最小销售包装单位)", requiredMode = Schema.RequiredMode.REQUIRED, example = "5596")
    private BigDecimal sellPackPrice;

    @Schema(description = "销售价格(最小制剂单位)", requiredMode = Schema.RequiredMode.REQUIRED, example = "20773")
    private BigDecimal sellDosagePrice;

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

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}