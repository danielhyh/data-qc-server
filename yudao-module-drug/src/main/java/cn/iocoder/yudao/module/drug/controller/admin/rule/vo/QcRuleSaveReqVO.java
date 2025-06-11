package cn.iocoder.yudao.module.drug.controller.admin.rule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 质控规则新增/修改 Request VO")
@Data
public class QcRuleSaveReqVO {

    @Schema(description = "规则ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6894")
    private Long id;

    @Schema(description = "规则编码（如PRE_QC_001）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "规则编码（如PRE_QC_001）不能为空")
    private String ruleCode;

    @Schema(description = "规则名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotEmpty(message = "规则名称不能为空")
    private String ruleName;

    @Schema(description = "规则类型:1-前置质控,2-后置质控", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "规则类型:1-前置质控,2-后置质控不能为空")
    private Integer ruleType;

    @Schema(description = "规则分类:GLOBAL-全局,FIELD-字段,LOGIC-逻辑")
    private String ruleCategory;

    @Schema(description = "适用表:1-机构,2-目录,3-入库,4-出库,5-使用,NULL-全部", example = "2")
    private Integer tableType;

    @Schema(description = "检查字段名", example = "芋艿")
    private String fieldName;

    @Schema(description = "规则表达式（支持SpEL）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "规则表达式（支持SpEL）不能为空")
    private String ruleExpression;

    @Schema(description = "错误提示信息模板", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "错误提示信息模板不能为空")
    private String errorMessage;

    @Schema(description = "错误级别:1-错误,2-警告", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "错误级别:1-错误,2-警告不能为空")
    private Integer errorLevel;

    @Schema(description = "阈值配置（JSON格式）")
    private String thresholdValue;

    @Schema(description = "优先级（越小越优先）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "优先级（越小越优先）不能为空")
    private Integer priority;

    @Schema(description = "是否启用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否启用不能为空")
    private Boolean enabled;

    @Schema(description = "规则详细说明", example = "你猜")
    private String description;

}