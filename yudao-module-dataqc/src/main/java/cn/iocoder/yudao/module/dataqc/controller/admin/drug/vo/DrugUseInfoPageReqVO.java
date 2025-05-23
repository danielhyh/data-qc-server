package cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 药品使用情况分页 Request VO")
@Data
public class DrugUseInfoPageReqVO extends PageParam {

    @Schema(description = "流水号")
    private Long serialNum;

    @Schema(description = "系统编码")
    private String domainCode;

    @Schema(description = "组织机构代码")
    private String organizationCode;

    @Schema(description = "组织机构名称", example = "李四")
    private String organizationName;

    @Schema(description = "医疗机构代码")
    private String hospitalCode;

    @Schema(description = "数据上报日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private String[] uploadDate;

    @Schema(description = "国家药管平台药品编码", example = "4317")
    private String ypid;

    @Schema(description = "省级药品集中采购平台药品编码", example = "14829")
    private String prDrugId;

    @Schema(description = "院内药品唯一码", example = "29079")
    private String hosDrugId;

    @Schema(description = "产品通用名", example = "芋艿")
    private String productName;

    @Schema(description = "药品销售日期(yyyyMMdd)")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private String[] sellDate;

    @Schema(description = "销售价格(最小销售包装单位)", example = "5596")
    private BigDecimal sellPackPrice;

    @Schema(description = "销售数量(最小销售包装单位)")
    private Long sellPackQuantity;

    @Schema(description = "销售价格(最小制剂单位)", example = "20773")
    private BigDecimal sellDosagePrice;

    @Schema(description = "销售数量(最小制剂单位)")
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
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] importTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}