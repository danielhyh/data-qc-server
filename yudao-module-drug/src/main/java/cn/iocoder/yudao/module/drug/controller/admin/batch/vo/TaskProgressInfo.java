package cn.iocoder.yudao.module.drug.controller.admin.batch.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 任务进度信息
 * <p>
 * 设计理念：轻量级的进度跟踪数据结构，专门用于Redis缓存
 * 包含了前端实时显示所需的核心信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskProgressInfo {
    
    /**
     * 任务ID - 用于关联具体任务
     */
    private Long taskId;
    
    /**
     * 当前进度百分比
     * 0 表示执行失败
     */
    private int progress;
    
    /**
     * 当前状态描述信息
     * 例如："正在解压文件..."、"正在处理药品目录..."等
     */
    private String message;
    
    /**
     * 当前处理阶段
     * 例如：EXTRACTING, IMPORTING, QC_CHECKING等
     */
    private String currentStage;
    
    /**
     * 预计剩余时间（秒）
     * 基于当前进度和历史数据计算得出
     */
    private Long estimatedRemainingSeconds;
    
    /**
     * 最后更新时间
     * 用于判断进度是否还在活跃更新
     */
    private LocalDateTime updateTime;
    
    /**
     * 扩展信息Map
     * 用于存储一些动态的统计信息，避免频繁修改实体结构
     */
    private Map<String, Object> extraInfo;
}