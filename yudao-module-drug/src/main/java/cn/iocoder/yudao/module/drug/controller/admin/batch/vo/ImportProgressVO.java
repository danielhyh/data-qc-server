// ==================== 导入进度信息 ====================
package cn.iocoder.yudao.module.drug.controller.admin.batch.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 药品导入进度信息
 * 设计理念：多层次进度展示，支持实时监控和用户交互
 */
@Data
@Builder
public class ImportProgressVO {
    
    // ========== 基础任务信息 ==========
    private Long taskId;
    private String taskNo;
    private String taskName;
    
    // ========== 整体进度信息 ==========
    private Integer overallStatus;          // 任务整体状态
    private int overallProgress;        // 整体进度百分比
    private String currentMessage;          // 当前状态描述
    private String currentStage;            // 当前处理阶段
    private Long estimatedRemainingTime;    // 预计剩余时间（秒）
    
    // ========== 时间信息 ==========
    private LocalDateTime startTime;        // 开始时间
    private LocalDateTime estimatedEndTime; // 预计结束时间
    private LocalDateTime lastUpdateTime;   // 最后更新时间
    
    // ========== 统计信息 ==========
    private Integer totalFiles;             // 总文件数
    private Integer successFiles;           // 成功文件数
    private Integer failedFiles;            // 失败文件数
    private Long totalRecords;              // 总记录数
    private Long successRecords;            // 成功记录数
    private Long failedRecords;             // 失败记录数
    
    // ========== 表级进度信息 ==========
    private List<TableProgressVO> tableProgress;   // 各表处理进度
    
    // ========== 操作控制信息 ==========
    private Boolean canRetry;               // 是否可以重试
    private Boolean canCancel;              // 是否可以取消
    private List<String> availableRetryTypes; // 可用的重试类型
    
    // ========== 错误信息 ==========
    private String errorMessage;           // 错误消息
    private Map<String, Object> errorDetail; // 详细错误信息
}