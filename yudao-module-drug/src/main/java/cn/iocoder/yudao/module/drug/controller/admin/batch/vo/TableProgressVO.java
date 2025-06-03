package cn.iocoder.yudao.module.drug.controller.admin.batch.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 表进度信息视图对象
 * <p>
 * 用于前端展示单个表的处理进度
 * 这个类体现了"前后端分离"的设计理念，专门为前端展示优化
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableProgressVO {
    
    /**
     * 表类型标识
     */
    private Integer tableType;
    
    /**
     * 表显示名称
     * 面向用户的友好名称，如"机构基本信息"
     */
    private String tableName;
    
    /**
     * 处理状态
     * 使用数字编码便于前端状态判断和样式渲染
     */
    private Integer status;
    
    /**
     * 处理进度百分比 (0-100)
     * -1表示处理失败
     */
    private Integer progress;
    
    /**
     * 当前状态描述
     * 动态的状态信息，如"正在解析第1000行数据..."
     */
    private String currentMessage;
    
    /**
     * 总记录数
     */
    private Long totalRecords;
    
    /**
     * 成功处理记录数
     */
    private Long successRecords;
    
    /**
     * 失败记录数
     */
    private Long failedRecords;
    
    /**
     * 开始处理时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束处理时间
     */
    private LocalDateTime endTime;
    
    /**
     * 预计剩余时间（秒）
     * null表示无法估算
     */
    private Long estimatedRemainingSeconds;
    
    /**
     * 是否可以重试
     * 用于前端按钮状态控制
     */
    private Boolean canRetry;
    
    /**
     * 处理速度（记录/秒）
     * 用于性能监控
     */
    private Double processingSpeed;
}