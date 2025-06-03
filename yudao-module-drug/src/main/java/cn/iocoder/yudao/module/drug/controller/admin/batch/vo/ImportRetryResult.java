// ==================== 重试结果 ====================
package cn.iocoder.yudao.module.drug.controller.admin.batch.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 导入重试结果
 * 设计理念：提供重试操作的完整反馈信息
 */
@Data
@Builder
public class ImportRetryResult {
    
    private Long taskId;
    private String taskNo;
    private Boolean success;               // 重试操作是否成功启动
    private String message;               // 结果消息
    
    private String retryType;             // 重试类型
    private List<String> retryScope;      // 重试范围（表类型列表）
    
    private LocalDateTime retryStartTime; // 重试开始时间
    private String retryBatchNo;          // 重试批次号
    
    // 重试前状态统计
    private RetryStatistics beforeRetry;
    
    @Data
    @Builder
    public static class RetryStatistics {
        private Integer failedTables;      // 失败表数量
        private Long failedRecords;       // 失败记录数
        private String lastFailureReason; // 最后失败原因
    }
}