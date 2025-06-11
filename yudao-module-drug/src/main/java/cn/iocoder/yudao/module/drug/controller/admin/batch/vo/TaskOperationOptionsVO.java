package cn.iocoder.yudao.module.drug.controller.admin.batch.vo;// ==================== 7. 操作选项VO ====================

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 任务操作选项VO
 * <p>
 * 设计理念：根据任务当前状态动态提供可用的操作选项
 * 为前端提供操作按钮的显示控制逻辑
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskOperationOptionsVO {
    
    /**
     * 基本操作权限
     */
    private BasicOperations basicOps;
    
    /**
     * 重试相关操作
     */
    private RetryOperations retryOps;
    
    /**
     * 导出相关操作
     */
    private ExportOperations exportOps;
    
    /**
     * 基本操作内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BasicOperations {
        private Boolean canCancel;
        private Boolean canViewLogs;
        private Boolean canViewDetails;
        private Boolean canDelete;
    }
    
    /**
     * 重试操作内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetryOperations {
        private Boolean canRetry;
        private List<String> availableRetryTypes;
        private Long estimatedRetryDuration;  // 预计重试耗时（秒）
        private String retryRecommendation;   // 重试建议
    }
    
    /**
     * 导出操作内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExportOperations {
        private Boolean canDownloadReport;
        private Boolean canExportData;
        private Boolean canExportErrors;
        private List<String> availableExportFormats; // ["EXCEL", "PDF", "CSV"]
    }
}