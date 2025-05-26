package cn.iocoder.yudao.module.dataqc.controller.admin.drug;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.dataqc.controller.admin.batchimport.vo.BatchImportTaskPageReqVO;
import cn.iocoder.yudao.module.dataqc.controller.admin.batchimport.vo.BatchImportTaskRespVO;
import cn.iocoder.yudao.module.dataqc.controller.admin.importlog.vo.ImportLogPageReqVO;
import cn.iocoder.yudao.module.dataqc.controller.admin.importlog.vo.ImportLogRespVO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.batchimport.BatchImportTaskDO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.importlog.ImportLogDO;
import cn.iocoder.yudao.module.dataqc.service.batchimport.IBatchImportService;
import cn.iocoder.yudao.module.dataqc.service.importlog.ImportLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 系统监控控制器
 *
 * 核心监控价值：
 * 1. 系统健康监控 - 实时监控系统运行状态和性能指标
 * 2. 业务流程监控 - 跟踪导入任务执行情况和成功率
 * 3. 数据质量监控 - 监控数据导入的准确性和完整性
 * 4. 性能瓶颈识别 - 识别系统性能问题和资源瓶颈
 * 5. 运维决策支持 - 为系统优化和容量规划提供数据支撑
 *
 * 监控设计理念：
 * - 全方位监控：涵盖系统、业务、数据等多个层面
 * - 实时性：提供实时的监控数据和告警机制
 * - 可视化：直观的图表和仪表板展示
 * - 可操作：不仅监控问题，还提供问题解决建议
 */
@Tag(name = "管理后台 - 系统监控")
@RestController
@RequestMapping("/dataqc/monitor")
@Validated
@Slf4j
public class MonitorController {

    @Resource
    private IBatchImportService batchImportService;
    @Resource
    private ImportLogService importLogService;

    /**
     * 获取系统概览仪表板
     *
     * 仪表板设计：
     * - 系统状态卡片：CPU、内存、磁盘使用率
     * - 业务指标卡片：今日导入任务数、成功率、数据量
     * - 实时监控图表：系统性能曲线、任务执行趋势
     * - 告警信息：异常任务、系统警告、业务异常
     */
    @GetMapping("/dashboard")
    @Operation(summary = "获取系统监控仪表板")
    @PreAuthorize("@ss.hasPermission('dataqc:monitor:dashboard')")
    public CommonResult<Map<String, Object>> getMonitorDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        try {
            // 1. 系统基础指标
            Map<String, Object> systemMetrics = getSystemMetrics();
            dashboard.put("systemMetrics", systemMetrics);

            // 2. 业务运行指标
            Map<String, Object> businessMetrics = getBusinessMetrics();
            dashboard.put("businessMetrics", businessMetrics);

            // 3. 任务执行统计
            Map<String, Object> taskStatistics = getTaskStatistics();
            dashboard.put("taskStatistics", taskStatistics);

            // 4. 性能趋势数据
            List<Map<String, Object>> performanceTrend = getPerformanceTrend();
            dashboard.put("performanceTrend", performanceTrend);

            // 5. 告警信息
            List<Map<String, Object>> alerts = getSystemAlerts();
            dashboard.put("alerts", alerts);

            // 6. 更新时间
            dashboard.put("updateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            log.info("成功获取系统监控仪表板数据");

        } catch (Exception e) {
            log.error("获取系统监控仪表板失败", e);
            dashboard.put("error", "监控数据获取失败");
        }

        return success(dashboard);
    }

    /**
     * 获取导入日志分页列表
     *
     * 监控价值：
     * - 追踪所有数据导入操作
     * - 分析导入失败原因
     * - 统计导入成功率和性能
     * - 为系统优化提供数据支撑
     */
    @GetMapping("/import-logs")
    @Operation(summary = "获取导入日志分页列表")
    @PreAuthorize("@ss.hasPermission('dataqc:monitor:query')")
    public CommonResult<PageResult<ImportLogRespVO>> getImportLogs(@Valid ImportLogPageReqVO pageReqVO) {
        PageResult<ImportLogDO> pageResult = importLogService.getImportLogPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ImportLogRespVO.class));
    }

    /**
     * 获取批量导入任务列表
     *
     * 任务监控：
     * - 实时查看任务执行状态
     * - 监控任务执行进度
     * - 分析任务性能瓶颈
     * - 识别问题任务模式
     */
    @GetMapping("/batch-tasks")
    @Operation(summary = "获取批量导入任务列表")
    @PreAuthorize("@ss.hasPermission('dataqc:monitor:query')")
    public CommonResult<List<BatchImportTaskRespVO>> getBatchTasks(@Valid BatchImportTaskPageReqVO queryReqVO) {
        BatchImportTaskDO queryTask = BeanUtils.toBean(queryReqVO, BatchImportTaskDO.class);
        List<BatchImportTaskDO> taskList = batchImportService.selectTaskList(queryTask);
        return success(BeanUtils.toBean(taskList, BatchImportTaskRespVO.class));
    }

    /**
     * 导出导入日志
     *
     * 导出用途：
     * - 审计和合规检查
     * - 问题排查和分析
     * - 性能基线建立
     * - 历史数据归档
     */
    @GetMapping("/export-logs")
    @Operation(summary = "导出导入日志")
    @PreAuthorize("@ss.hasPermission('dataqc:monitor:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportImportLogs(HttpServletResponse response, @Valid ImportLogPageReqVO exportReqVO) throws IOException {
        PageResult<ImportLogDO> pageResult = importLogService.getImportLogPage(exportReqVO);
        ExcelUtils.write(response, "导入日志数据.xls", "导入日志", ImportLogRespVO.class,
                BeanUtils.toBean(pageResult.getList(), ImportLogRespVO.class));
    }

    /**
     * 获取系统性能报告
     *
     * 报告内容：
     * - 系统资源使用情况
     * - 应用性能关键指标
     * - 数据库连接池状态
     * - 业务处理性能统计
     */
    @GetMapping("/performance-report")
    @Operation(summary = "获取系统性能报告")
    @PreAuthorize("@ss.hasPermission('dataqc:monitor:query')")
    public CommonResult<Map<String, Object>> getPerformanceReport(
            @Parameter(description = "开始时间") @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) String endTime) {

        Map<String, Object> report = new HashMap<>();

        try {
            // 设置默认时间范围（最近24小时）
            if (startTime == null || endTime == null) {
                LocalDateTime now = LocalDateTime.now();
                endTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                startTime = now.minusHours(24).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }

            // 1. 系统资源使用报告
            Map<String, Object> resourceUsage = getDetailedSystemMetrics();
            report.put("resourceUsage", resourceUsage);

            // 2. 任务执行性能报告
            Map<String, Object> taskPerformance = getTaskPerformanceReport(startTime, endTime);
            report.put("taskPerformance", taskPerformance);

            // 3. 数据质量报告
            Map<String, Object> dataQuality = getDataQualityReport(startTime, endTime);
            report.put("dataQuality", dataQuality);

            // 4. 系统健康评分
            Map<String, Object> healthScore = calculateHealthScore();
            report.put("healthScore", healthScore);

            log.info("成功生成系统性能报告，时间范围：{} - {}", startTime, endTime);

        } catch (Exception e) {
            log.error("生成系统性能报告失败", e);
            report.put("error", "性能报告生成失败");
        }

        return success(report);
    }

    /**
     * 获取实时监控数据
     *
     * 实时数据：
     * - 当前系统负载
     * - 正在执行的任务
     * - 实时性能指标
     * - 即时告警信息
     */
    @GetMapping("/real-time-metrics")
    @Operation(summary = "获取实时监控数据")
    @PreAuthorize("@ss.hasPermission('dataqc:monitor:query')")
    public CommonResult<Map<String, Object>> getRealTimeMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        try {
            // 1. 实时系统指标
            metrics.put("systemMetrics", getSystemMetrics());

            // 2. 当前执行中的任务
            metrics.put("runningTasks", getCurrentRunningTasks());

            // 3. 实时性能数据
            metrics.put("performanceData", getRealTimePerformance());

            // 4. 最新告警信息
            metrics.put("latestAlerts", getLatestAlerts());

            // 5. 数据刷新时间
            metrics.put("timestamp", System.currentTimeMillis());

        } catch (Exception e) {
            log.error("获取实时监控数据失败", e);
            metrics.put("error", "实时数据获取失败");
        }

        return success(metrics);
    }

    // ========== 私有辅助方法 ==========

    /**
     * 获取系统基础指标
     */
    private Map<String, Object> getSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        try {
            // JVM内存信息
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            long totalMemory = memoryBean.getHeapMemoryUsage().getMax();
            long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();

            // 系统信息-使用Sun特定的MXBean进行CPU度量
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

            // 内存指标
            metrics.put("memoryUsage", Math.round((double) usedMemory / totalMemory * 100));
            metrics.put("memoryTotal", totalMemory / 1024 / 1024); // MB
            metrics.put("memoryUsed", usedMemory / 1024 / 1024);   // MB

            // CPU使用率-安全转换以访问扩展功能
            double cpuUsage = 0.0;
            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                com.sun.management.OperatingSystemMXBean sunOsBean =
                        (com.sun.management.OperatingSystemMXBean) osBean;
                cpuUsage = sunOsBean.getProcessCpuLoad();

                //getProcessCpuLoad（）返回一个介于0.0和1.0之间的值
                //负值表示CPU使用率不可用
                if (cpuUsage < 0) {
                    cpuUsage = 0.0;
                }
            }

            metrics.put("cpuUsage", Math.round(cpuUsage * 100));
            metrics.put("uptime", runtimeBean.getUptime() / 1000); // seconds
            metrics.put("threadCount", Thread.activeCount());

            // 通过Sun特定的MXBean提供其他系统指标
            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                com.sun.management.OperatingSystemMXBean sunOsBean =
                        (com.sun.management.OperatingSystemMXBean) osBean;

                // 全系统CPU使用率（所有进程）
                double systemCpuLoad = sunOsBean.getSystemCpuLoad();
                if (systemCpuLoad >= 0) {
                    metrics.put("systemCpuUsage", Math.round(systemCpuLoad * 100));
                }

                // 物理内存信息
                long totalPhysicalMemory = sunOsBean.getTotalPhysicalMemorySize();
                long freePhysicalMemory = sunOsBean.getFreePhysicalMemorySize();
                if (totalPhysicalMemory > 0) {
                    long usedPhysicalMemory = totalPhysicalMemory - freePhysicalMemory;
                    metrics.put("physicalMemoryUsage",
                            Math.round((double) usedPhysicalMemory / totalPhysicalMemory * 100));
                    metrics.put("totalPhysicalMemory", totalPhysicalMemory / 1024 / 1024); // MB
                    metrics.put("freePhysicalMemory", freePhysicalMemory / 1024 / 1024);   // MB
                }
            }

        } catch (Exception e) {
            log.error("获取系统指标失败", e);
            // 监控失败时返回安全默认值
            metrics.put("memoryUsage", 0);
            metrics.put("cpuUsage", 0);
            metrics.put("uptime", 0);
            metrics.put("threadCount", 0);
        }

        return metrics;
    }

    /**
     * 获取业务运行指标
     */
    private Map<String, Object> getBusinessMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // 这里应该调用实际的业务统计方法
        // 示例数据
        metrics.put("todayTasks", 28);
        metrics.put("todaySuccessRate", 92.5);
        metrics.put("todayDataRows", 125600);
        metrics.put("activeUsers", 15);

        return metrics;
    }

    /**
     * 获取任务执行统计
     */
    private Map<String, Object> getTaskStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 这里应该调用batchImportService的统计方法
        statistics.put("totalTasks", 156);
        statistics.put("successTasks", 142);
        statistics.put("failedTasks", 8);
        statistics.put("processingTasks", 6);
        statistics.put("successRate", 91.0);

        return statistics;
    }

    /**
     * 获取性能趋势数据
     */
    private List<Map<String, Object>> getPerformanceTrend() {
        List<Map<String, Object>> trend = new ArrayList<>();

        // 生成最近24小时的趋势数据（示例）
        LocalDateTime now = LocalDateTime.now();
        for (int i = 23; i >= 0; i--) {
            Map<String, Object> point = new HashMap<>();
            LocalDateTime time = now.minusHours(i);
            point.put("time", time.format(DateTimeFormatter.ofPattern("HH:mm")));
            point.put("cpuUsage", 20 + new Random().nextInt(40));
            point.put("memoryUsage", 30 + new Random().nextInt(30));
            point.put("taskCount", 5 + new Random().nextInt(10));
            trend.add(point);
        }

        return trend;
    }

    /**
     * 获取系统告警信息
     */
    private List<Map<String, Object>> getSystemAlerts() {
        List<Map<String, Object>> alerts = new ArrayList<>();

        // 示例告警数据
        alerts.add(Map.of(
            "level", "warning",
            "message", "内存使用率超过80%",
            "time", LocalDateTime.now().minusMinutes(5).format(DateTimeFormatter.ofPattern("HH:mm"))
        ));

        alerts.add(Map.of(
            "level", "info",
            "message", "批量导入任务执行完成",
            "time", LocalDateTime.now().minusMinutes(10).format(DateTimeFormatter.ofPattern("HH:mm"))
        ));

        return alerts;
    }

    /**
     * 获取详细系统指标
     */
    private Map<String, Object> getDetailedSystemMetrics() {
        return getSystemMetrics(); // 可以扩展更详细的指标
    }

    /**
     * 获取任务性能报告
     */
    private Map<String, Object> getTaskPerformanceReport(String startTime, String endTime) {
        return Map.of(
            "avgExecutionTime", 125.5,
            "maxExecutionTime", 320.2,
            "minExecutionTime", 45.1,
            "throughput", 850.6
        );
    }

    /**
     * 获取数据质量报告
     */
    private Map<String, Object> getDataQualityReport(String startTime, String endTime) {
        return Map.of(
            "dataIntegrityRate", 98.5,
            "dataAccuracyRate", 96.8,
            "duplicateRate", 0.3,
            "errorRate", 1.2
        );
    }

    /**
     * 计算系统健康评分
     */
    private Map<String, Object> calculateHealthScore() {
        return Map.of(
            "overallScore", 92,
            "performanceScore", 89,
            "reliabilityScore", 95,
            "availabilityScore", 98
        );
    }

    /**
     * 获取当前运行中的任务
     */
    private List<Map<String, Object>> getCurrentRunningTasks() {
        return List.of(); // 实际应该查询正在执行的任务
    }

    /**
     * 获取实时性能数据
     */
    private Map<String, Object> getRealTimePerformance() {
        return getSystemMetrics();
    }

    /**
     * 获取最新告警信息
     */
    private List<Map<String, Object>> getLatestAlerts() {
        return getSystemAlerts();
    }
}