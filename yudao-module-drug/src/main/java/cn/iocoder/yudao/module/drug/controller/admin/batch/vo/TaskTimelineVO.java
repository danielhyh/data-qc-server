package cn.iocoder.yudao.module.drug.controller.admin.batch.vo;// ==================== 5. 时间线VO ====================

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 任务时间线VO
 * <p>
 * 设计理念：提供任务执行过程的关键节点记录
 * 支持多种事件类型和展示样式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskTimelineVO {
    
    /**
     * 事件时间戳
     */
    private LocalDateTime timestamp;
    
    /**
     * 事件代码
     */
    private String event;
    
    /**
     * 事件标题
     */
    private String title;
    
    /**
     * 事件描述
     */
    private String description;
    
    /**
     * 事件类型：info, primary, success, warning, error
     */
    private String type;
    
    /**
     * 关联的表类型（可选）
     */
    private String relatedTableType;
    
    /**
     * 耗时信息（可选）
     */
    private Long durationMs;
    
    /**
     * 额外信息
     */
    private Map<String, Object> extraInfo;
}