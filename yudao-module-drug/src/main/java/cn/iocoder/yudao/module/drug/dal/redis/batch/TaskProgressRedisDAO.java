package cn.iocoder.yudao.module.drug.dal.redis.batch;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.drug.controller.admin.batch.vo.ImportSessionInfo;
import cn.iocoder.yudao.module.drug.controller.admin.batch.vo.TaskDetailProgressInfo;
import cn.iocoder.yudao.module.drug.controller.admin.batch.vo.TaskProgressInfo;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.module.drug.dal.redis.RedisKeyConstants.*;

/**
 * 任务进度相关的 Redis 数据访问层 - 增强版
 * <p>
 * 设计理念：
 * 1. 职责集中：统一管理任务相关的所有Redis操作，包括编号生成
 * 2. 原子操作：利用Redis的原子性保证数据一致性
 * 3. 性能优化：支持批量操作，合理设置过期时间
 * 4. 可扩展性：预留扩展接口，支持未来的功能增强
 *
 * @author yourname
 * @since 2024-06-03
 */
@Repository
public class TaskProgressRedisDAO {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // ==================== 任务编号生成 ====================

    /**
     * 生成唯一的任务编号
     * <p>
     * 设计理念：
     * - 使用Redis原子递增确保编号唯一性
     * - 按日期分片，避免序号过大且便于数据清理
     * - 统一格式便于运维查询和问题定位
     * 
     * 格式：DRUG_YYYYMMDD_XXXXXX
     * 示例：DRUG_20240603_000001
     *
     * @return 唯一任务编号
     */
    public String generateTaskNo() {
        LocalDateTime now = LocalDateTime.now();
        String dateStr = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 使用Redis原子递增生成序号，避免并发冲突
        String redisKey = "drug:task:sequence:" + dateStr;
        Long sequence = stringRedisTemplate.opsForValue().increment(redisKey);
        
        // 设置当天过期，第二天自动重置序号从1开始
        stringRedisTemplate.expire(redisKey, Duration.ofDays(1));

        return String.format("DRUG_%s_%06d", dateStr, sequence);
    }

    /**
     * 生成重试批次编号
     * <p>
     * 用于标识重试操作的批次，便于追踪重试历史
     *
     * @param taskId     原任务ID
     * @param retryType  重试类型
     * @return 重试批次编号
     */
    public String generateRetryBatchNo(Long taskId, String retryType) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String redisKey = "drug:retry:sequence:" + dateStr;
        Long sequence = stringRedisTemplate.opsForValue().increment(redisKey);
        
        // 重试序号过期时间设置为1小时
        stringRedisTemplate.expire(redisKey, Duration.ofHours(1));
        
        return String.format("RETRY_%d_%s_%s_%04d", taskId, retryType, dateStr, sequence);
    }

    // ==================== 任务总体进度相关操作 ====================

    /**
     * 获取任务进度信息
     * <p>
     * 这是前端轮询查询的核心方法，需要确保高效性
     *
     * @param taskId 任务ID
     * @return 任务进度信息，如果不存在则返回null
     */
    public TaskProgressInfo getTaskProgress(Long taskId) {
        String redisKey = formatTaskProgressKey(taskId);
        String jsonValue = stringRedisTemplate.opsForValue().get(redisKey);
        return JsonUtils.parseObject(jsonValue, TaskProgressInfo.class);
    }

    /**
     * 设置任务进度信息
     * <p>
     * 优化策略：
     * - 清理不必要字段减少内存占用
     * - 强制更新时间确保数据时效性
     * - 合理的过期时间平衡实时性和资源消耗
     *
     * @param progressInfo 任务进度信息
     */
    public void setTaskProgress(TaskProgressInfo progressInfo) {
        String redisKey = formatTaskProgressKey(progressInfo.getTaskId());

        // 构建优化后的进度信息，只保留必要字段
        TaskProgressInfo cleanedInfo = TaskProgressInfo.builder()
                .taskId(progressInfo.getTaskId())
                .progress(progressInfo.getProgress())
                .message(progressInfo.getMessage())
                .currentStage(progressInfo.getCurrentStage())
                .estimatedRemainingSeconds(progressInfo.getEstimatedRemainingSeconds())
                .updateTime(LocalDateTime.now()) // 强制更新为当前时间
                .extraInfo(progressInfo.getExtraInfo())
                .build();

        // 设置30分钟过期时间，足够支持大部分导入任务
        stringRedisTemplate.opsForValue().set(
                redisKey,
                JsonUtils.toJsonString(cleanedInfo),
                30,
                TimeUnit.MINUTES
        );
    }

    /**
     * 删除任务进度信息
     * <p>
     * 通常在任务完成或被取消时调用
     *
     * @param taskId 任务ID
     */
    public void deleteTaskProgress(Long taskId) {
        String redisKey = formatTaskProgressKey(taskId);
        stringRedisTemplate.delete(redisKey);
    }

    /**
     * 批量删除任务进度信息
     * <p>
     * 用于清理过期或已完成的任务进度，提高Redis性能
     *
     * @param taskIds 任务ID集合
     */
    public void deleteTaskProgressBatch(Collection<Long> taskIds) {
        if (CollectionUtils.isAnyEmpty(taskIds)) {
            return;
        }

        List<String> redisKeys = taskIds.stream()
                .map(this::formatTaskProgressKey)
                .collect(Collectors.toList());

        stringRedisTemplate.delete(redisKeys);
    }

    // ==================== 任务明细进度相关操作 ====================

    /**
     * 获取任务明细进度信息
     * <p>
     * 用于查询特定表或文件的处理进度
     *
     * @param taskId    任务ID
     * @param tableType 表类型
     * @return 明细进度信息
     */
    public TaskDetailProgressInfo getTaskDetailProgress(Long taskId, String tableType) {
        String redisKey = formatTaskDetailProgressKey(taskId, tableType);
        String jsonValue = stringRedisTemplate.opsForValue().get(redisKey);
        return JsonUtils.parseObject(jsonValue, TaskDetailProgressInfo.class);
    }

    /**
     * 设置任务明细进度信息
     *
     * @param detailProgressInfo 明细进度信息
     */
    public void setTaskDetailProgress(TaskDetailProgressInfo detailProgressInfo) {
        String redisKey = formatTaskDetailProgressKey(
                detailProgressInfo.getTaskId(),
                detailProgressInfo.getTableType()
        );

        // 更新时间标记
        detailProgressInfo.setUpdateTime(LocalDateTime.now());

        // 明细进度信息也设置30分钟过期
        stringRedisTemplate.opsForValue().set(
                redisKey,
                JsonUtils.toJsonString(detailProgressInfo),
                30,
                TimeUnit.MINUTES
        );
    }

    /**
     * 获取任务的所有明细进度信息
     * <p>
     * 性能注意事项：
     * - 使用SCAN代替KEYS避免阻塞Redis
     * - 在生产环境中建议改用Hash结构存储明细进度
     *
     * @param taskId 任务ID
     * @return 所有明细进度信息的Map，key为tableType
     */
    public Map<String, TaskDetailProgressInfo> getAllTaskDetailProgress(Long taskId) {
        String pattern = formatTaskDetailProgressKey(taskId, "*");

        // TODO: 生产环境建议使用SCAN替代keys，避免Redis阻塞
        return stringRedisTemplate.keys(pattern).stream()
                .collect(Collectors.toMap(
                        this::extractTableTypeFromKey,
                        key -> {
                            String jsonValue = stringRedisTemplate.opsForValue().get(key);
                            return JsonUtils.parseObject(jsonValue, TaskDetailProgressInfo.class);
                        }
                ));
    }

    /**
     * 删除任务的所有明细进度信息
     *
     * @param taskId 任务ID
     */
    public void deleteAllTaskDetailProgress(Long taskId) {
        String pattern = formatTaskDetailProgressKey(taskId, "*");
        stringRedisTemplate.delete(stringRedisTemplate.keys(pattern));
    }

    // ==================== 任务锁相关操作 ====================

    /**
     * 尝试获取任务锁
     * <p>
     * 分布式锁实现：
     * - 使用setIfAbsent保证原子性
     * - 设置过期时间防止死锁
     * - 记录锁的所有者便于验证
     *
     * @param taskId 任务ID
     * @param userId 操作用户ID
     * @return 是否成功获取锁
     */
    public boolean tryLockTask(Long taskId, Long userId) {
        String redisKey = formatTaskLockKey(taskId);

        // 使用setIfAbsent实现分布式锁，过期时间5分钟
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(redisKey, userId.toString(), 5, TimeUnit.MINUTES);

        return Boolean.TRUE.equals(success);
    }

    /**
     * 释放任务锁
     * <p>
     * 安全释放：只有锁的所有者才能释放锁
     *
     * @param taskId 任务ID
     * @param userId 操作用户ID（用于验证锁的所有者）
     * @return 是否成功释放
     */
    public boolean unlockTask(Long taskId, Long userId) {
        String redisKey = formatTaskLockKey(taskId);
        String currentLockOwner = stringRedisTemplate.opsForValue().get(redisKey);

        // 只有锁的所有者才能释放锁，防止误释放
        if (userId.toString().equals(currentLockOwner)) {
            stringRedisTemplate.delete(redisKey);
            return true;
        }

        return false;
    }

    // ==================== 导入会话相关操作 ====================

    /**
     * 获取导入会话信息
     *
     * @param sessionId 会话ID
     * @return 会话信息
     */
    public ImportSessionInfo getImportSession(String sessionId) {
        String redisKey = formatImportSessionKey(sessionId);
        String jsonValue = stringRedisTemplate.opsForValue().get(redisKey);
        return JsonUtils.parseObject(jsonValue, ImportSessionInfo.class);
    }

    /**
     * 设置导入会话信息
     * <p>
     * 会话信息过期时间设置为1小时，支持中长期的断点续传
     *
     * @param sessionInfo 会话信息
     */
    public void setImportSession(ImportSessionInfo sessionInfo) {
        String redisKey = formatImportSessionKey(sessionInfo.getSessionId());

        // 更新最后活跃时间
        sessionInfo.setLastActiveTime(LocalDateTime.now());

        stringRedisTemplate.opsForValue().set(
                redisKey,
                JsonUtils.toJsonString(sessionInfo),
                1,
                TimeUnit.HOURS
        );
    }

    /**
     * 删除导入会话信息
     *
     * @param sessionId 会话ID
     */
    public void deleteImportSession(String sessionId) {
        String redisKey = formatImportSessionKey(sessionId);
        stringRedisTemplate.delete(redisKey);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 格式化任务进度的Redis键
     */
    private String formatTaskProgressKey(Long taskId) {
        return String.format(DRUG_TASK_PROGRESS, taskId);
    }

    /**
     * 格式化任务明细进度的Redis键
     */
    private String formatTaskDetailProgressKey(Long taskId, String tableType) {
        return String.format(DRUG_TASK_DETAIL_PROGRESS, taskId, tableType);
    }

    /**
     * 格式化任务锁的Redis键
     */
    private String formatTaskLockKey(Long taskId) {
        return String.format(DRUG_TASK_LOCK, taskId);
    }

    /**
     * 格式化导入会话的Redis键
     */
    private String formatImportSessionKey(String sessionId) {
        return String.format(DRUG_IMPORT_SESSION, sessionId);
    }

    /**
     * 从Redis键中提取表类型
     * <p>
     * 这是一个辅助方法，用于从Redis键名中解析出tableType
     * 键格式假设为: "drug:task:detail:{taskId}:{tableType}"
     */
    private String extractTableTypeFromKey(String redisKey) {
        String[] parts = redisKey.split(":");
        return parts.length > 4 ? parts[4] : "";
    }
}