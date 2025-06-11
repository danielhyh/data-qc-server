package cn.iocoder.yudao.module.drug.dal.dataobject.rule;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 质控规则 DO
 *
 * @author hyh
 */
@TableName("drug_qc_rule")
@KeySequence("drug_qc_rule_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QcRuleDO extends BaseDO {

    /**
     * 规则ID
     */
    @TableId
    private Long id;
    /**
     * 规则编码（如PRE_QC_001）
     */
    private String ruleCode;
    /**
     * 规则名称
     */
    private String ruleName;
    /**
     * 规则类型:1-前置质控,2-后置质控
     */
    private Integer ruleType;
    /**
     * 规则分类:GLOBAL-全局,FIELD-字段,LOGIC-逻辑
     */
    private String ruleCategory;
    /**
     * 适用表:1-机构,2-目录,3-入库,4-出库,5-使用,NULL-全部
     */
    private Integer tableType;
    /**
     * 检查字段名
     */
    private String fieldName;
    /**
     * 规则表达式（支持SpEL）
     */
    private String ruleExpression;
    /**
     * 错误提示信息模板
     */
    private String errorMessage;
    /**
     * 错误级别:1-错误,2-警告
     */
    private Integer errorLevel;
    /**
     * 阈值配置（JSON格式）
     */
    private String thresholdValue;
    /**
     * 优先级（越小越优先）
     */
    private Integer priority;
    /**
     * 是否启用
     */
    private Boolean enabled;
    /**
     * 规则详细说明
     */
    private String description;


}