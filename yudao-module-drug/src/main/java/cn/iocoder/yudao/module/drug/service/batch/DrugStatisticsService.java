package cn.iocoder.yudao.module.drug.service.batch;

import cn.iocoder.yudao.module.drug.controller.admin.batch.vo.ImportStatisticsVO;
import cn.iocoder.yudao.module.drug.dal.dataobject.batch.ImportTaskDO;
import cn.iocoder.yudao.module.drug.dal.mysql.batch.ImportTaskMapper;
import cn.iocoder.yudao.module.drug.enums.TaskStatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 药品导入统计分析服务
 * <p>
 * 设计理念：
 * 1. 数据驱动：基于实际导入数据提供准确的统计分析
 * 2. 性能优化：使用并行计算和缓存机制提升查询效率
 * 3. 灵活查询：支持多维度的时间范围和条件筛选
 * 4. 实时更新：提供实时和历史数据的对比分析
 * 5. 业务洞察：不仅提供数据，更提供业务价值的分析结果
 */
@Service
@Slf4j
public class DrugStatisticsService {

    private final ExecutorService statisticsExecutor = Executors.newFixedThreadPool(4);
    @Resource
    private ImportTaskMapper importTaskMapper;

    /**
     * 获取导入统计信息
     * <p>
     * 提供全面的导入任务统计分析，支持时间范围筛选
     */
    public ImportStatisticsVO getImportStatistics(String startDate, String endDate) {
        log.info("开始计算导入统计信息: startDate={}, endDate={}", startDate, endDate);

        // 解析时间范围
        LocalDateTime startTime = parseDate(startDate, true);
        LocalDateTime endTime = parseDate(endDate, false);

        // 并行计算各项统计指标
        CompletableFuture<TaskCountStatistics> taskCountFuture =
                CompletableFuture.supplyAsync(() -> calculateTaskCountStatistics(startTime, endTime), statisticsExecutor);

        CompletableFuture<ProcessingStatistics> processingFuture =
                CompletableFuture.supplyAsync(() -> calculateProcessingStatistics(startTime, endTime), statisticsExecutor);

        CompletableFuture<TrendStatistics> trendFuture =
                CompletableFuture.supplyAsync(() -> calculateTrendStatistics(startTime, endTime), statisticsExecutor);

        try {
            // 等待所有计算完成
            TaskCountStatistics taskCount = taskCountFuture.get();
            ProcessingStatistics processing = processingFuture.get();
            TrendStatistics trend = trendFuture.get();

            // 构建统计结果
            ImportStatisticsVO statistics = ImportStatisticsVO.builder()
                    .totalTasks(taskCount.getTotalTasks())
                    .successTasks(taskCount.getSuccessTasks())
                    .failedTasks(taskCount.getFailedTasks())
                    .partialSuccessTasks(taskCount.getPartialSuccessTasks())
                    .runningTasks(taskCount.getRunningTasks())
                    .successRate(taskCount.getSuccessRate())
                    .averageProcessingTime(processing.getAverageProcessingTime())
                    .totalRecordsProcessed(processing.getTotalRecordsProcessed())
                    .todayTasks(trend.getTodayTasks())
                    .yesterdayTasks(trend.getYesterdayTasks())
                    .taskGrowthRate(trend.getTaskGrowthRate())
                    .build();

            log.info("统计信息计算完成: totalTasks={}, successRate={}%",
                    statistics.getTotalTasks(), statistics.getSuccessRate());

            return statistics;

        } catch (Exception e) {
            log.error("计算统计信息时发生异常", e);
            return createEmptyStatistics();
        }
    }

    /**
     * 计算任务数量统计
     */
    private TaskCountStatistics calculateTaskCountStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            LambdaQueryWrapper<ImportTaskDO> baseWrapper = new LambdaQueryWrapper<ImportTaskDO>()
                    .between(ImportTaskDO::getCreateTime, startTime, endTime);

            // 查询总任务数
            Long totalTasks = importTaskMapper.selectCount(baseWrapper);

            // 按状态分组统计
            Map<Integer, Long> statusCountMap = importTaskMapper.selectList(baseWrapper)
                    .stream()
                    .collect(Collectors.groupingBy(
                            ImportTaskDO::getStatus,
                            Collectors.counting()
                    ));

            Long successTasks = statusCountMap.getOrDefault(TaskStatusEnum.COMPLETED.getStatus(), 0L);
            Long failedTasks = statusCountMap.getOrDefault(TaskStatusEnum.FAILED.getStatus(), 0L);
            Long partialSuccessTasks = statusCountMap.getOrDefault(TaskStatusEnum.PARTIAL_SUCCESS.getStatus(), 0L);
            Long runningTasks = statusCountMap.entrySet().stream()
                    .filter(entry -> TaskStatusEnum.valueOf(String.valueOf(entry.getKey())).isProcessing())
                    .mapToLong(Map.Entry::getValue)
                    .sum();

            // 计算成功率
            Double successRate = totalTasks > 0 ?
                    BigDecimal.valueOf((successTasks + partialSuccessTasks) * 100.0 / totalTasks)
                            .setScale(2, RoundingMode.HALF_UP)
                            .doubleValue() : 0.0;

            return new TaskCountStatistics(totalTasks.intValue(), successTasks.intValue(),
                    failedTasks.intValue(), partialSuccessTasks.intValue(),
                    runningTasks.intValue(), successRate);

        } catch (Exception e) {
            log.error("计算任务数量统计时发生异常", e);
            return new TaskCountStatistics(0, 0, 0, 0, 0, 0.0);
        }
    }

    /**
     * 计算处理性能统计
     */
    private ProcessingStatistics calculateProcessingStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            List<ImportTaskDO> completedTasks = importTaskMapper.selectList(
                    new LambdaQueryWrapper<ImportTaskDO>()
                            .between(ImportTaskDO::getCreateTime, startTime, endTime)
                            .in(ImportTaskDO::getStatus,
                                    TaskStatusEnum.COMPLETED.getStatus(),
                                    TaskStatusEnum.FAILED.getStatus(),
                                    TaskStatusEnum.PARTIAL_SUCCESS.getStatus())
                            .isNotNull(ImportTaskDO::getStartTime)
                            .isNotNull(ImportTaskDO::getEndTime)
            );

            if (completedTasks.isEmpty()) {
                return new ProcessingStatistics(0, 0L);
            }

            // 计算平均处理时间
            double averageProcessingTime = completedTasks.stream()
                    .mapToLong(task -> {
                        LocalDateTime start = task.getStartTime();
                        LocalDateTime end = task.getEndTime();
                        return java.time.Duration.between(start, end).getSeconds();
                    })
                    .average()
                    .orElse(0.0);

            // 计算总处理记录数
            Long totalRecordsProcessed = completedTasks.stream()
                    .mapToLong(task -> task.getTotalRecords() != null ? task.getTotalRecords() : 0L)
                    .sum();

            return new ProcessingStatistics((int) averageProcessingTime, totalRecordsProcessed);

        } catch (Exception e) {
            log.error("计算处理性能统计时发生异常", e);
            return new ProcessingStatistics(0, 0L);
        }
    }

    /**
     * 计算趋势统计
     */
    private TrendStatistics calculateTrendStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);

            // 查询今日任务数
            Integer todayTasks = importTaskMapper.selectCount(
                    new LambdaQueryWrapper<ImportTaskDO>()
                            .between(ImportTaskDO::getCreateTime,
                                    today.atStartOfDay(),
                                    today.atTime(23, 59, 59))
            ).intValue();

            // 查询昨日任务数
            Integer yesterdayTasks = importTaskMapper.selectCount(
                    new LambdaQueryWrapper<ImportTaskDO>()
                            .between(ImportTaskDO::getCreateTime,
                                    yesterday.atStartOfDay(),
                                    yesterday.atTime(23, 59, 59))
            ).intValue();

            // 计算增长率
            Double growthRate = 0.0;
            if (yesterdayTasks > 0) {
                growthRate = BigDecimal.valueOf((todayTasks - yesterdayTasks) * 100.0 / yesterdayTasks)
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();
            } else if (todayTasks > 0) {
                growthRate = 100.0; // 昨日为0，今日有数据，则为100%增长
            }

            return new TrendStatistics(todayTasks, yesterdayTasks, growthRate);

        } catch (Exception e) {
            log.error("计算趋势统计时发生异常", e);
            return new TrendStatistics(0, 0, 0.0);
        }
    }

    /**
     * 解析日期字符串
     */
    private LocalDateTime parseDate(String dateStr, boolean isStart) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            if (isStart) {
                // 默认开始时间为30天前
                return LocalDate.now().minusDays(30).atStartOfDay();
            } else {
                // 默认结束时间为当前时间
                return LocalDateTime.now();
            }
        }

        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return isStart ? date.atStartOfDay() : date.atTime(23, 59, 59);
        } catch (Exception e) {
            log.warn("日期解析失败，使用默认值: dateStr={}", dateStr, e);
            return isStart ? LocalDate.now().minusDays(30).atStartOfDay() : LocalDateTime.now();
        }
    }

    /**
     * 创建空的统计结果
     */
    private ImportStatisticsVO createEmptyStatistics() {
        return ImportStatisticsVO.builder()
                .totalTasks(0)
                .successTasks(0)
                .failedTasks(0)
                .partialSuccessTasks(0)
                .runningTasks(0)
                .successRate(0.0)
                .averageProcessingTime(0)
                .totalRecordsProcessed(0L)
                .todayTasks(0)
                .yesterdayTasks(0)
                .taskGrowthRate(0.0)
                .build();
    }

    // 内部统计数据类
    private static class TaskCountStatistics {
        private final Integer totalTasks;
        private final Integer successTasks;
        private final Integer failedTasks;
        private final Integer partialSuccessTasks;
        private final Integer runningTasks;
        private final Double successRate;

        public TaskCountStatistics(Integer totalTasks, Integer successTasks, Integer failedTasks,
                                   Integer partialSuccessTasks, Integer runningTasks, Double successRate) {
            this.totalTasks = totalTasks;
            this.successTasks = successTasks;
            this.failedTasks = failedTasks;
            this.partialSuccessTasks = partialSuccessTasks;
            this.runningTasks = runningTasks;
            this.successRate = successRate;
        }

        // Getters
        public Integer getTotalTasks() {
            return totalTasks;
        }

        public Integer getSuccessTasks() {
            return successTasks;
        }

        public Integer getFailedTasks() {
            return failedTasks;
        }

        public Integer getPartialSuccessTasks() {
            return partialSuccessTasks;
        }

        public Integer getRunningTasks() {
            return runningTasks;
        }

        public Double getSuccessRate() {
            return successRate;
        }
    }

    private static class ProcessingStatistics {
        private final Integer averageProcessingTime;
        private final Long totalRecordsProcessed;

        public ProcessingStatistics(Integer averageProcessingTime, Long totalRecordsProcessed) {
            this.averageProcessingTime = averageProcessingTime;
            this.totalRecordsProcessed = totalRecordsProcessed;
        }

        public Integer getAverageProcessingTime() {
            return averageProcessingTime;
        }

        public Long getTotalRecordsProcessed() {
            return totalRecordsProcessed;
        }
    }

    private static class TrendStatistics {
        private final Integer todayTasks;
        private final Integer yesterdayTasks;
        private final Double taskGrowthRate;

        public TrendStatistics(Integer todayTasks, Integer yesterdayTasks, Double taskGrowthRate) {
            this.todayTasks = todayTasks;
            this.yesterdayTasks = yesterdayTasks;
            this.taskGrowthRate = taskGrowthRate;
        }

        public Integer getTodayTasks() {
            return todayTasks;
        }

        public Integer getYesterdayTasks() {
            return yesterdayTasks;
        }

        public Double getTaskGrowthRate() {
            return taskGrowthRate;
        }
    }
}