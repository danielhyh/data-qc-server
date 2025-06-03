package cn.iocoder.yudao.module.drug.controller.admin.batch.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 任务明细进度信息
 * <p>
 * 用于跟踪单个表或文件的处理进度
 * 提供更细粒度的进度控制
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDetailProgressInfo {
    
    /**
     * 任务ID
     */
    private Long taskId;
    
    /**
     * 表类型或文件类型
     */
    private String tableType;
    
    /**
     * 当前进度百分比 (0-100)
     */
    private int progress;
    
    /**
     * 状态描述
     */
    private String message;
    
    /**
     * 处理状态
     * WAITING, PROCESSING, SUCCESS, FAILED, PARTIAL_SUCCESS
     */
    private String status;
    
    /**
     * 总记录数
     */
    private Integer totalRecords;
    
    /**
     * 已处理记录数
     */
    private Integer processedRecords;
    
    /**
     * 成功记录数
     */
    private Integer successRecords;
    
    /**
     * 失败记录数
     */
    private Integer failedRecords;
    
    /**
     * 错误信息（如果有的话）
     */
    private String errorMessage;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}