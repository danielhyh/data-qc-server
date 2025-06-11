// ==================== 1. 任务基本信息VO ====================
package cn.iocoder.yudao.module.drug.controller.admin.batch.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 任务基本信息VO
 * <p>
 * 设计理念：仅包含任务的核心识别信息和元数据
 * 避免与进度、统计等业务逻辑混合
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskInfoVO {
    
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
     * 原始文件名
     */
    private String fileName;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 任务状态
     */
    private Integer status;
    
    /**
     * 状态显示文本
     */
    private String statusDisplay;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 创建人
     */
    private String creator;
    
    /**
     * 数据来源
     */
    private String dataSource;
    
    /**
     * 任务描述
     */
    private String description;
}