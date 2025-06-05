package cn.iocoder.yudao.module.drug.controller.admin.batch.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 任务详情返回对象
 */
@Data
@Builder
public class ImportTaskDetailVO {
    
    /**
     * 任务基本信息
     */
    private TaskInfoVO taskInfo;
    
    /**
     * 整体进度信息
     */
    private TaskOverallProgressVO overallProgress;
    
    /**
     * 表级别详情列表
     */
    private List<TableDetailVO> tableDetails;
    
    /**
     * 统计信息
     */
    private TaskStatisticsVO statistics;
    
    /**
     * 任务时间线
     */
    private List<TaskTimelineVO> timeline;
    
    /**
     * 最近日志
     */
    private List<TaskLogVO> recentLogs;
    
    /**
     * 相关任务
     */
    private List<RelatedTaskVO> relatedTasks;
    
    /**
     * 操作选项
     */
    private TaskOperationOptionsVO operationOptions;
    
    /**
     * 质量报告
     */
    private TaskQualityReportVO qualityReport;
}