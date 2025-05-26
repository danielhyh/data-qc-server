package cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - 药品使用情况 Response VO")
@Data
@ExcelIgnoreUnannotated
public class DrugUseInfoRespVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "19890")
    @ExcelProperty("主键ID")
    private Long id;

    @Schema(description = "流水号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("流水号")
    private Long serialNum;

    @Schema(description = "系统编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("系统编码")
    private String domainCode;

    @Schema(description = "组织机构代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("组织机构代码")
    private String organizationCode;

    @Schema(description = "组织机构名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @ExcelProperty("组织机构名称")
    private String organizationName;

    @Schema(description = "医疗机构代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("医疗机构代码")
    private String hospitalCode;

    @Schema(description = "数据上报日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("数据上报日期")
    private String uploadDate;

    @Schema(description = "国家药管平台药品编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "4317")
    @ExcelProperty("国家药管平台药品编码")
    private String ypid;

    @Schema(description = "省级药品集中采购平台药品编码", example = "14829")
    @ExcelProperty("省级药品集中采购平台药品编码")
    private String prDrugId;

    @Schema(description = "院内药品唯一码", requiredMode = Schema.RequiredMode.REQUIRED, example = "29079")
    @ExcelProperty("院内药品唯一码")
    private String hosDrugId;

    @Schema(description = "产品通用名", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @ExcelProperty("产品通用名")
    private String productName;

    @Schema(description = "药品销售日期(yyyyMMdd)", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("药品销售日期(yyyyMMdd)")
    private String sellDate;

    @Schema(description = "销售价格(最小销售包装单位)", requiredMode = Schema.RequiredMode.REQUIRED, example = "5596")
    @ExcelProperty("销售价格(最小销售包装单位)")
    private BigDecimal sellPackPrice;

    @Schema(description = "销售数量(最小销售包装单位)", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("销售数量(最小销售包装单位)")
    private Long sellPackQuantity;

    @Schema(description = "销售价格(最小制剂单位)", requiredMode = Schema.RequiredMode.REQUIRED, example = "20773")
    @ExcelProperty("销售价格(最小制剂单位)")
    private BigDecimal sellDosagePrice;

    @Schema(description = "销售数量(最小制剂单位)", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("销售数量(最小制剂单位)")
    private Long sellDosageQuantity;

    @Schema(description = "使用科室代码")
    @ExcelProperty("使用科室代码")
    private String departmentCode;

    @Schema(description = "使用科室名称", example = "王五")
    @ExcelProperty("使用科室名称")
    private String departmentName;

    @Schema(description = "开具医生代码")
    @ExcelProperty("开具医生代码")
    private String doctorCode;

    @Schema(description = "开具医生姓名", example = "王五")
    @ExcelProperty("开具医生姓名")
    private String doctorName;

    @Schema(description = "患者类型(门诊/住院)", example = "2")
    @ExcelProperty("患者类型(门诊/住院)")
    private String patientType;

    @Schema(description = "导入批次号")
    @ExcelProperty("导入批次号")
    private String importBatchNo;

    @Schema(description = "导入时间")
    @ExcelProperty("导入时间")
    private LocalDateTime importTime;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}