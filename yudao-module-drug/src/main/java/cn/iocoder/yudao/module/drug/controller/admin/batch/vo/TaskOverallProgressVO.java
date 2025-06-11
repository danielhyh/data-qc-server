package cn.iocoder.yudao.module.drug.controller.admin.batch.vo;// ==================== 2. 整体进度VO ====================

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 任务整体进度VO
 * <p>
 * 设计理念：专注于整体进度展示，不包含表级别的细节
 * 为前端提供统一的进度展示接口
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskOverallProgressVO {
    
    /**
     * 整体进度百分比 (0-100)
     */
    private Integer overallProgress;
    
    /**
     * 当前阶段
     */
    private String currentStage;
    
    /**
     * 当前状态描述
     */
    private String currentMessage;
    
    /**
     * 各阶段状态
     */
    private StageStatusInfo stageStatus;
    
    /**
     * 时间信息
     */
    private TimeInfo timeInfo;
    
    /**
     * 预计信息
     */
    private EstimationInfo estimation;
    
    /**
     * 阶段状态信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StageStatusInfo {
        private Integer extractStatus;      // 解压状态
        private Integer importStatus;       // 导入状态  
        private Integer qcStatus;          // 质控状态
    }
    
    /**
     * 时间信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeInfo {
        private LocalDateTime startTime;
        private LocalDateTime estimatedEndTime;
        private LocalDateTime lastUpdateTime;
        private Long elapsedSeconds;
    }
    
    /**
     * 预估信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EstimationInfo {
        private Long estimatedRemainingSeconds;
        private Double processingSpeed;     // 记录/秒
        private String speedDisplay;       // 格式化的速度显示
    }
}