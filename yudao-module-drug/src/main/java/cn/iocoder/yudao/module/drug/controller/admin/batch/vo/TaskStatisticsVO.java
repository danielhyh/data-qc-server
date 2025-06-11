package cn.iocoder.yudao.module.drug.controller.admin.batch.vo;// ==================== 4. 统计信息VO（重构现有的） ====================

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 任务统计信息VO
 * <p>
 * 重构说明：整合了多个统计相关的VO，提供分层的统计信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatisticsVO {
    
    /**
     * 文件统计
     */
    private FileStatistics fileStats;
    
    /**
     * 记录统计  
     */
    private RecordStatistics recordStats;
    
    /**
     * 性能统计
     */
    private PerformanceStatistics performanceStats;
    
    /**
     * 质量统计
     */
    private QualityStatistics qualityStats;
    
    /**
     * 文件统计内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileStatistics {
        private Integer totalFiles;
        private Integer successFiles;
        private Integer failedFiles;
        private Double fileSuccessRate;
        private Map<String, Integer> fileCountByType;
    }
    
    /**
     * 记录统计内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecordStatistics {
        private Long totalRecords;
        private Long successRecords;
        private Long failedRecords;
        private Double overallSuccessRate;
        private Map<String, Long> recordCountByType;
    }
    
    /**
     * 性能统计内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceStatistics {
        private Double averageProcessingSpeed;  // 记录/秒
        private Long estimatedTimeRemaining;    // 剩余秒数
        private Integer averageProcessingTime;   // 平均处理时间（秒）
        private String performanceLevel;        // HIGH/MEDIUM/LOW
    }
    
    /**
     * 质量统计内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QualityStatistics {
        private Map<String, Double> fileSuccessRateByType;
        private Map<String, Double> recordSuccessRateByType;
        private Map<String, Integer> qualityScoreDistribution;
        private Double averageQualityScore;
    }
}
