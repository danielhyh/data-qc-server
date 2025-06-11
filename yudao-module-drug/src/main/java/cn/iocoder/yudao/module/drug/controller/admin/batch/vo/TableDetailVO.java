package cn.iocoder.yudao.module.drug.controller.admin.batch.vo;// ==================== 3. 表级详情VO（重构TableProgressVO） ====================

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 表级详情VO
 * <p>
 * 重构说明：整合了原来的TableProgressVO和ImportTaskDetailItemVO
 * 提供表级别的完整信息，包括进度、统计、时间等
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableDetailVO {
    
    /**
     * 表基本信息
     */
    private TableBasicInfo basicInfo;
    
    /**
     * 进度信息  
     */
    private TableProgressInfo progressInfo;
    
    /**
     * 统计信息
     */
    private TableStatisticsInfo statisticsInfo;
    
    /**
     * 操作信息
     */
    private TableOperationInfo operationInfo;
    
    /**
     * 表基本信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TableBasicInfo {
        private Integer tableType;
        private String tableName;
        private String fileName;
        private String fileType;
        private Long fileSize;
    }
    
    /**
     * 表进度信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TableProgressInfo {
        private Integer status;
        private String statusDisplay;
        private Integer progressPercent;
        private String currentMessage;
        private Integer parseStatus;
        private Integer importStatus;
        private Integer qcStatus;
    }
    
    /**
     * 表统计信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TableStatisticsInfo {
        private Long totalRows;
        private Long validRows;
        private Long successRows;
        private Long failedRows;
        private Long qcPassedRows;
        private Long qcFailedRows;
        private Double successRate;
    }
    
    /**
     * 表操作信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TableOperationInfo {
        private Boolean canRetry;
        private Integer retryCount;
        private Integer maxRetryCount;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String errorMessage;
    }
}