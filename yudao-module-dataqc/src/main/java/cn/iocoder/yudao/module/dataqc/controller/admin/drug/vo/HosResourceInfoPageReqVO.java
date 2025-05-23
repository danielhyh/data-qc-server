package cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 医疗机构资源信息分页 Request VO")
@Data
public class HosResourceInfoPageReqVO extends PageParam {

    @Schema(description = "系统编码")
    private String domainCode;

    @Schema(description = "组织机构代码")
    private String organizationCode;

    @Schema(description = "组织机构名称", example = "张三")
    private String organizationName;

    @Schema(description = "医疗机构代码")
    private String hospitalCode;

    @Schema(description = "统计日期(yyyyMMdd)")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private String[] statDate;

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

    @Schema(description = "数据上报日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private String[] uploadDate;

    @Schema(description = "导入批次号")
    private String importBatchNo;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}