package cn.iocoder.yudao.module.drug.controller.admin.batch.vo;// ==================== 8. 质量报告VO ====================

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 任务质量报告VO
 * <p>
 * 设计理念：提供全面的数据质量分析结果
 * 包含评分、问题分析和改进建议
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskQualityReportVO {
    
    /**
     * 质量评分信息
     */
    private QualityScores scores;
    
    /**
     * 质量问题分析
     */
    private QualityIssues issues;
    
    /**
     * 改进建议
     */
    private QualityRecommendations recommendations;
    
    /**
     * 详细指标
     */
    private Map<String, Object> detailedMetrics;
    
    /**
     * 质量评分内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QualityScores {
        private Double overallQualityScore;     // 整体质量得分
        private Double dataIntegrityScore;      // 数据完整性得分
        private Double consistencyScore;        // 一致性得分
        private Double completenessScore;       // 完整性得分
        private String overallGrade;           // 整体等级 A/B/C/D
    }
    
    /**
     * 质量问题内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QualityIssues {
        private List<QualityIssueDetail> criticalIssues;  // 严重问题
        private List<QualityIssueDetail> warningIssues;   // 警告问题
        private List<QualityIssueDetail> infoIssues;      // 信息问题
        private Integer totalIssueCount;
    }
    
    /**
     * 质量问题详情内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QualityIssueDetail {
        private String issueType;              // 问题类型
        private String issueDescription;       // 问题描述
        private String affectedTable;          // 影响的表
        private Integer affectedRecords;       // 影响的记录数
        private String severity;               // 严重程度：CRITICAL/WARNING/INFO
    }
    
    /**
     * 改进建议内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QualityRecommendations {
        private List<String> immediateActions;    // 立即行动建议
        private List<String> processImprovements; // 流程改进建议
        private List<String> preventiveMeasures;  // 预防措施建议
        private String overallSuggestion;        // 整体建议
    }
}
