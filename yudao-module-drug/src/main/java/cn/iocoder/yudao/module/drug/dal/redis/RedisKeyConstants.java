package cn.iocoder.yudao.module.drug.dal.redis;

/**
 * System Redis Key 枚举类
 *
 * @author hyh
 */
public interface RedisKeyConstants {

    // ==================== 药品导入相关缓存键 ====================
    
    /**
     * 药品导入任务进度的缓存
     * <p>
     * KEY 格式：drug:task:progress:{taskId}
     * VALUE 数据类型：String 任务进度信息 {@link TaskProgressInfo}
     * <p>
     * 过期时间：30分钟，用于实时进度跟踪
     */
    String DRUG_TASK_PROGRESS = "drug:task:progress:%s";

    /**
     * 药品导入任务详细状态的缓存
     * <p>
     * KEY 格式：drug:task:detail:{taskId}:{tableType}
     * VALUE 数据类型：String 任务明细状态信息 {@link TaskDetailProgressInfo}
     * <p>
     * 过期时间：30分钟，用于表级别进度跟踪
     */
    String DRUG_TASK_DETAIL_PROGRESS = "drug:task:detail:%s:%s";

    /**
     * 药品导入任务锁的缓存
     * <p>
     * KEY 格式：drug:task:lock:{taskId}
     * VALUE 数据类型：String 锁信息（当前操作用户ID）
     * <p>
     * 过期时间：5分钟，防止并发操作冲突
     */
    String DRUG_TASK_LOCK = "drug:task:lock:%s";

    /**
     * 药品导入会话状态的缓存
     * <p>
     * KEY 格式：drug:import:session:{sessionId}
     * VALUE 数据类型：String 导入会话信息 {@link ImportSessionInfo}
     * <p>
     * 过期时间：1小时，用于断点续传和状态恢复
     */
    String DRUG_IMPORT_SESSION = "drug:import:session:%s";

}