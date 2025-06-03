package cn.iocoder.yudao.module.drug.controller.admin.batch.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 任务明细项VO
 * 
 * 单个表处理的详细信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ImportTaskDetailItemVO {
    
    /**
     * 明细ID
     */
    private Long id;
    
    /**
     * 任务ID
     */
    private Long taskId;
    
    /**
     * 文件类型
     */
    private String fileType;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 目标表名
     */
    private String targetTable;
    
    /**
     * 表类型
     */
    private Integer tableType;
    
    /**
     * 表名称
     */
    private String tableName;
    
    /**
     * 状态
     */
    private Integer status;
    
    /**
     * 状态显示文本
     */
    private String statusDisplay;
    
    /**
     * 解析状态
     */
    private Integer parseStatus;
    
    /**
     * 导入状态
     */
    private Integer importStatus;
    
    /**
     * 质控状态
     */
    private Integer qcStatus;
    
    /**
     * 总行数
     */
    private Long totalRows;
    
    /**
     * 有效行数
     */
    private Long validRows;
    
    /**
     * 成功行数
     */
    private Long successRows;
    
    /**
     * 失败行数
     */
    private Long failedRows;
    
    /**
     * 质控通过行数
     */
    private Long qcPassedRows;
    
    /**
     * 质控失败行数
     */
    private Long qcFailedRows;
    
    /**
     * 进度百分比
     */
    private Integer progressPercent;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 重试次数
     */
    private Integer retryCount;
    
    /**
     * 最大重试次数
     */
    private Integer maxRetryCount;
}