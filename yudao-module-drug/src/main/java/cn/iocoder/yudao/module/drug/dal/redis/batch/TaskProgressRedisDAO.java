package cn.iocoder.yudao.module.drug.dal.redis.batch;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.drug.dal.dataobject.batch.ImportSessionInfo;
import cn.iocoder.yudao.module.drug.dal.dataobject.batch.TaskDetailProgressInfo;
import cn.iocoder.yudao.module.drug.dal.dataobject.batch.TaskProgressInfo;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.module.drug.dal.redis.RedisKeyConstants.*;

/**
 * 任务进度相关的 Redis 数据访问层
 * <p>
 * 设计理念：
 * 1. 职责分离：专门处理任务进度相关的缓存操作，与业务逻辑解耦
 * 2. 统一管理：集中管理所有任务进度相关的Redis操作，便于维护和优化
 * 3. 性能优化：合理设置过期时间，避免缓存堆积；支持批量操作，减少网络往返
 * 4. 数据一致性：确保缓存数据的时效性和准确性
 *
 * @author yourname
 * @since 2024-06-03
 */
@Repository
public class TaskProgressRedisDAO {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

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
     * 在保存前会清理一些不必要的字段，减少Redis内存占用
     * 过期时间设置为30分钟，平衡了实时性和资源消耗
     *
     * @param progressInfo 任务进度信息
     */
    public void setTaskProgress(TaskProgressInfo progressInfo) {
        String redisKey = formatTaskProgressKey(progressInfo.getTaskId());

        // 清理不必要的字段，优化缓存大小
        // 注意：这里我们不清理updateTime，因为它对判断进度活跃性很重要
        TaskProgressInfo cleanedInfo = TaskProgressInfo.builder()
                .taskId(progressInfo.getTaskId())
                .progress(progressInfo.getProgress())
                .message(progressInfo.getMessage())
                .currentStage(progressInfo.getCurrentStage())
                .estimatedRemainingSeconds(progressInfo.getEstimatedRemainingSeconds())
                .updateTime(LocalDateTime.now()) // 强制更新为当前时间
                .extraInfo(progressInfo.getExtraInfo())
                .build();

        // 设置30分钟过期时间，这个时间足够长，可以支持大部分导入任务
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
     * 这个方法通过模式匹配来查找所有相关的明细进度
     * 注意：在生产环境中，如果明细数量很大，建议改用Hash结构存储
     *
     * @param taskId 任务ID
     * @return 所有明细进度信息的Map，key为tableType
     */
    public Map<String, TaskDetailProgressInfo> getAllTaskDetailProgress(Long taskId) {
        String pattern = formatTaskDetailProgressKey(taskId, "*");

        return stringRedisTemplate.keys(pattern).stream()
                .collect(Collectors.toMap(
                        key -> extractTableTypeFromKey(key),
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
     * 用于防止同一个任务被并发操作，比如同时取消和重试
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
     *
     * @param taskId 任务ID
     * @param userId 操作用户ID（用于验证锁的所有者）
     * @return 是否成功释放
     */
    public boolean unlockTask(Long taskId, Long userId) {
        String redisKey = formatTaskLockKey(taskId);
        String currentLockOwner = stringRedisTemplate.opsForValue().get(redisKey);

        // 只有锁的所有者才能释放锁
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
     */
    private String extractTableTypeFromKey(String redisKey) {
        // 假设键格式是 "drug:task:detail:{taskId}:{tableType}"
        String[] parts = redisKey.split(":");
        return parts.length > 4 ? parts[4] : "";
    }
}