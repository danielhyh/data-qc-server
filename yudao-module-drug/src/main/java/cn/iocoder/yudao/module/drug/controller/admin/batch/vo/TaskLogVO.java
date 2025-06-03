package cn.iocoder.yudao.module.drug.controller.admin.batch.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务日志VO
 * 
 * 展示任务执行的详细日志信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class TaskLogVO {
    
    /**
     * 任务ID
     */
    private Long taskId;
    
    /**
     * 日志内容
     */
    private String logs;
    
    /**
     * 日志级别
     */
    private String logLevel;
    
    /**
     * 总行数
     */
    private Integer totalLines;
    
    /**
     * 最后更新时间
     */
    private Long lastUpdateTime;
    
    /**
     * 日志文件大小
     */
    private Long logFileSize;
    
    /**
     * 是否有更多日志
     */
    private Boolean hasMoreLogs;
}
