package cn.iocoder.yudao.module.drug.controller.admin.batch.vo;// ==================== 6. 相关任务VO ====================

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 相关任务VO
 * <p>
 * 设计理念：展示与当前任务相关的其他任务信息
 * 帮助用户了解任务间的关联关系
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelatedTaskVO {
    
    /**
     * 任务ID
     */
    private Long taskId;
    
    /**
     * 任务编号
     */
    private String taskNo;
    
    /**
     * 任务名称
     */
    private String taskName;
    
    /**
     * 关联类型：SAME_BATCH, SIMILAR_DATA, RETRY_OF, RETRIED_BY
     */
    private String relationType;
    
    /**
     * 关联描述
     */
    private String relationDescription;
    
    /**
     * 任务状态
     */
    private Integer status;
    
    /**
     * 状态显示
     */
    private String statusDisplay;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 相似度（0-100，仅对SIMILAR_DATA类型有效）
     */
    private Integer similarity;
}