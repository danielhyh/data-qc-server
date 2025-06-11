package cn.iocoder.yudao.module.drug.controller.admin.rule.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 质控规则 Response VO")
@Data
@ExcelIgnoreUnannotated
public class QcRuleRespVO {

    @Schema(description = "规则ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6894")
    @ExcelProperty("规则ID")
    private Long id;

    @Schema(description = "规则编码（如PRE_QC_001）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("规则编码（如PRE_QC_001）")
    private String ruleCode;

    @Schema(description = "规则名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @ExcelProperty("规则名称")
    private String ruleName;

    @Schema(description = "规则类型:1-前置质控,2-后置质控", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("规则类型:1-前置质控,2-后置质控")
    private Integer ruleType;

    @Schema(description = "规则分类:GLOBAL-全局,FIELD-字段,LOGIC-逻辑")
    @ExcelProperty("规则分类:GLOBAL-全局,FIELD-字段,LOGIC-逻辑")
    private String ruleCategory;

    @Schema(description = "适用表:1-机构,2-目录,3-入库,4-出库,5-使用,NULL-全部", example = "2")
    @ExcelProperty("适用表:1-机构,2-目录,3-入库,4-出库,5-使用,NULL-全部")
    private Integer tableType;

    @Schema(description = "检查字段名", example = "芋艿")
    @ExcelProperty("检查字段名")
    private String fieldName;

    @Schema(description = "规则表达式（支持SpEL）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("规则表达式（支持SpEL）")
    private String ruleExpression;

    @Schema(description = "错误提示信息模板", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("错误提示信息模板")
    private String errorMessage;

    @Schema(description = "错误级别:1-错误,2-警告", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("错误级别:1-错误,2-警告")
    private Integer errorLevel;

    @Schema(description = "阈值配置（JSON格式）")
    @ExcelProperty("阈值配置（JSON格式）")
    private String thresholdValue;

    @Schema(description = "优先级（越小越优先）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("优先级（越小越优先）")
    private Integer priority;

    @Schema(description = "是否启用", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("是否启用")
    private Boolean enabled;

    @Schema(description = "规则详细说明", example = "你猜")
    @ExcelProperty("规则详细说明")
    private String description;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}