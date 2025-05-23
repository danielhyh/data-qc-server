package cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 药品目录分页 Request VO")
@Data
public class DrugListPageReqVO extends PageParam {

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

    @Schema(description = "国家药管平台药品编码", example = "21630")
    private String ypid;

    @Schema(description = "省级药品集中采购平台药品编码", example = "21725")
    private String prDrugId;

    @Schema(description = "院内药品唯一码", example = "14027")
    private String hosDrugId;

    @Schema(description = "批准文号")
    private String approvalNum;

    @Schema(description = "品种通用名", example = "李四")
    private String drugName;

    @Schema(description = "产品通用名", example = "王五")
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

    @Schema(description = "制剂单位")
    private String dosageUnit;

    @Schema(description = "最小销售包装单位")
    private String packUnit;

    @Schema(description = "转换系数")
    private BigDecimal drugFactor;

    @Schema(description = "是否网上集中采购药品(1是2否)")
    private String unityPurchaseFlag;

    @Schema(description = "是否基本药物(1是2否)")
    private String baseFlag;

    @Schema(description = "是否通过一致性评价(1是2否)")
    private String uniformityFlag;

    @Schema(description = "导入批次号")
    private String importBatchNo;

    @Schema(description = "导入时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] importTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}