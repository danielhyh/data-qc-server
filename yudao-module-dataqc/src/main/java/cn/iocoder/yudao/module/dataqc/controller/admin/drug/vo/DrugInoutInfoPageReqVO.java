package cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 药品出入库分页 Request VO")
@Data
public class DrugInoutInfoPageReqVO extends PageParam {

    @Schema(description = "流水号")
    private Long serialNum;

    @Schema(description = "系统编码")
    private String domainCode;

    @Schema(description = "组织机构代码")
    private String organizationCode;

    @Schema(description = "组织机构名称", example = "芋艿")
    private String organizationName;

    @Schema(description = "医疗机构代码")
    private String hospitalCode;

    @Schema(description = "数据上报日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private String[] uploadDate;

    @Schema(description = "国家药管平台药品编码", example = "27389")
    private String ypid;

    @Schema(description = "省级药品集中采购平台药品编码", example = "5070")
    private String prDrugId;

    @Schema(description = "院内药品唯一码", example = "12699")
    private String hosDrugId;

    @Schema(description = "产品通用名", example = "王五")
    private String productName;

    @Schema(description = "出入库时间(yyyyMMdd)")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private String[] outInDate;

    @Schema(description = "出入库类型(IN-入库,OUT-出库)", example = "2")
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
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private String[] productionDate;

    @Schema(description = "有效期至")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private String[] expiryDate;

    @Schema(description = "导入批次号")
    private String importBatchNo;

    @Schema(description = "导入时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] importTime;

    @Schema(description = "数据来源(IN_IMPORT-入库导入,OUT_IMPORT-出库导入)", example = "2")
    private String sourceType;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
    @Schema(description = "更新时间")
    private String beginDate;
    @Schema(description = "更新时间")
    private String endDate;

}