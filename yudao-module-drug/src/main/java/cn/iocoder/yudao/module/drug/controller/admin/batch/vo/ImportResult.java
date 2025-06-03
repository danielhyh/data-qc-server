// ==================== 导入结果 ====================
package cn.iocoder.yudao.module.drug.controller.admin.batch.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据导入结果
 * 设计理念：记录数据导入过程的详细结果，支持错误分析和数据回滚
 */
@Data
@Builder
public class ImportResult {
    
    private Boolean success;              // 整体是否成功
    private Boolean hasError;             // 是否有错误
    private String message;               // 结果消息
    
    private String tableType;             // 表类型
    private String importBatchNo;         // 导入批次号
    
    private Integer totalCount;           // 总数量
    private Integer successCount;         // 成功数量
    private Integer failedCount;          // 失败数量
    private Integer duplicateCount;       // 重复数量
    
    private LocalDateTime startTime;      // 开始时间
    private LocalDateTime endTime;        // 结束时间
    private Long processingTimeMs;        // 处理耗时（毫秒）
    
    private List<ImportError> importErrors; // 导入错误列表
    
    @Data
    @Builder
    public static class ImportError {
        private Integer batchIndex;       // 批次索引
        private Long recordId;           // 记录ID（如果有）
        private String errorType;        // 错误类型
        private String errorMessage;     // 错误描述
        private String errorDetail;      // 详细错误信息
    }
}