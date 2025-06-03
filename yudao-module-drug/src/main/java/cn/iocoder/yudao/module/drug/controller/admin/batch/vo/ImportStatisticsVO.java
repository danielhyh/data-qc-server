package cn.iocoder.yudao.module.drug.controller.admin.batch.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 导入统计信息VO
 * <p>
 * 提供全面的导入任务统计数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportStatisticsVO {

    /**
     * 总任务数
     */
    private Integer totalTasks;

    /**
     * 成功任务数
     */
    private Integer successTasks;

    /**
     * 失败任务数
     */
    private Integer failedTasks;

    /**
     * 部分成功任务数
     */
    private Integer partialSuccessTasks;

    /**
     * 正在运行的任务数
     */
    private Integer runningTasks;

    /**
     * 成功率（百分比）
     */
    private Double successRate;

    /**
     * 平均处理时间（秒）
     */
    private Integer averageProcessingTime;

    /**
     * 已处理的总记录数
     */
    private Long totalRecordsProcessed;

    /**
     * 今日任务数
     */
    private Integer todayTasks;

    /**
     * 昨日任务数
     */
    private Integer yesterdayTasks;

    /**
     * 任务增长率（百分比）
     */
    private Double taskGrowthRate;

    /**
     * 统计时间范围
     */
    private String statisticsTimeRange;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdateTime;
}