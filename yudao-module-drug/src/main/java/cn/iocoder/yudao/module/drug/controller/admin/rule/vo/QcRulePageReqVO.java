package cn.iocoder.yudao.module.drug.controller.admin.rule.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 质控规则分页 Request VO")
@Data
public class QcRulePageReqVO extends PageParam {

    @Schema(description = "规则编码（如PRE_QC_001）")
    private String ruleCode;

    @Schema(description = "规则名称", example = "李四")
    private String ruleName;

    @Schema(description = "规则类型:1-前置质控,2-后置质控", example = "1")
    private Integer ruleType;

    @Schema(description = "规则分类:GLOBAL-全局,FIELD-字段,LOGIC-逻辑")
    private String ruleCategory;

    @Schema(description = "适用表:1-机构,2-目录,3-入库,4-出库,5-使用,NULL-全部", example = "2")
    private Integer tableType;

    @Schema(description = "检查字段名", example = "芋艿")
    private String fieldName;

    @Schema(description = "规则表达式（支持SpEL）")
    private String ruleExpression;

    @Schema(description = "错误提示信息模板")
    private String errorMessage;

    @Schema(description = "错误级别:1-错误,2-警告")
    private Integer errorLevel;

    @Schema(description = "阈值配置（JSON格式）")
    private String thresholdValue;

    @Schema(description = "优先级（越小越优先）")
    private Integer priority;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "规则详细说明", example = "你猜")
    private String description;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}