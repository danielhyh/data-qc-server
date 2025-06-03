// ==================== 质控结果 ====================
package cn.iocoder.yudao.module.drug.controller.admin.batch.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 质控检查结果
 * 设计理念：全面的质控结果记录，支持质量分析和改进建议
 */
@Data
@Builder
public class QualityControlResult {
    
    private Boolean success;              // 质控是否通过
    private String overallGrade;          // 整体质量等级 A/B/C/D
    private Double overallScore;          // 整体质量得分 (0-100)
    
    private String qcType;                // 质控类型：PRE/POST/TABLE
    private String scope;                 // 质控范围
    
    private Integer totalRules;           // 总规则数
    private Integer passedRules;          // 通过规则数
    private Integer failedRules;          // 失败规则数
    
    private Long totalRecords;            // 总记录数
    private Long passedCount;             // 通过数量
    private Long failedCount;             // 失败数量
    private Long warningCount;            // 警告数量
    
    private LocalDateTime startTime;      // 开始时间
    private LocalDateTime endTime;        // 结束时间
    
    private List<QualityControlDetail> ruleResults; // 规则检查详情
    private Map<String, Object> statisticSummary;   // 统计摘要
    
    @Data
    @Builder
    public static class QualityControlDetail {
        private String ruleCode;          // 规则编码
        private String ruleName;          // 规则名称
        private String ruleType;          // 规则类型
        private Boolean passed;           // 是否通过
        private Integer affectedRecords;  // 影响记录数
        private String resultMessage;     // 结果描述
        private String suggestion;        // 改进建议
    }
}
