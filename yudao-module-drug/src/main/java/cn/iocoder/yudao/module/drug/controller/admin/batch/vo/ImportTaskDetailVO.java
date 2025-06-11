package cn.iocoder.yudao.module.drug.controller.admin.batch.vo;// ==================== 9. 重构后的主要详情VO ====================

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 导入任务详情VO - 重构版
 * <p>
 * 重构说明：
 * 1. 保持原有的字段结构，确保向后兼容
 * 2. 整合了多个相关VO的功能
 * 3. 提供了更清晰的数据组织结构
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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