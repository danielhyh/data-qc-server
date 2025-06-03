package cn.iocoder.yudao.module.drug.dal.dataobject.batch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 导入会话信息
 * <p>
 * 用于支持断点续传和会话恢复功能
 * 当系统重启或网络中断时，可以基于会话信息恢复任务状态
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportSessionInfo {
    
    /**
     * 会话ID - 通常使用UUID生成
     */
    private String sessionId;
    
    /**
     * 关联的任务ID
     */
    private Long taskId;
    
    /**
     * 用户ID - 发起导入的用户
     */
    private Long userId;
    
    /**
     * 会话状态
     * ACTIVE, PAUSED, COMPLETED, FAILED
     */
    private String sessionStatus;
    
    /**
     * 当前处理的文件或表
     */
    private String currentProcessingItem;
    
    /**
     * 已完成的处理项列表
     */
    private java.util.List<String> completedItems;
    
    /**
     * 待处理的项列表
     */
    private java.util.List<String> pendingItems;
    
    /**
     * 会话创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 最后活跃时间
     */
    private LocalDateTime lastActiveTime;
    
    /**
     * 会话元数据
     * 存储一些会话相关的配置信息或状态数据
     */
    private Map<String, Object> metadata;
}