package cn.iocoder.yudao.module.drug.convert.batch;

import cn.iocoder.yudao.module.drug.controller.admin.batch.vo.TableDetailVO;
import cn.iocoder.yudao.module.drug.controller.admin.batch.vo.TableProgressVO;
import cn.iocoder.yudao.module.drug.controller.admin.batch.vo.TaskInfoVO;
import cn.iocoder.yudao.module.drug.dal.dataobject.batch.ImportTaskDO;
import cn.iocoder.yudao.module.drug.enums.TaskStatusEnum;

/**
 * VO转换工具类
 * <p>
 * 提供新旧VO之间的转换方法，确保向后兼容性
 */
public class TaskDetailVOConverter {
    
    /**
     * 将新的TableDetailVO转换为旧的TableProgressVO（向后兼容）
     */
    public static TableProgressVO toTableProgressVO(TableDetailVO tableDetail) {
        return TableProgressVO.builder()
                .tableType(tableDetail.getBasicInfo().getTableType())
                .tableName(tableDetail.getBasicInfo().getTableName())
                .status(tableDetail.getProgressInfo().getStatus())
                .progress(tableDetail.getProgressInfo().getProgressPercent())
                .currentMessage(tableDetail.getProgressInfo().getCurrentMessage())
                .totalRecords(tableDetail.getStatisticsInfo().getTotalRows())
                .successRecords(tableDetail.getStatisticsInfo().getSuccessRows())
                .failedRecords(tableDetail.getStatisticsInfo().getFailedRows())
                .startTime(tableDetail.getOperationInfo().getStartTime())
                .endTime(tableDetail.getOperationInfo().getEndTime())
                .canRetry(tableDetail.getOperationInfo().getCanRetry())
                .build();
    }
    
    /**
     * 从ImportTaskDO构建TaskInfoVO
     */
    public static TaskInfoVO buildTaskInfoVO(ImportTaskDO task) {
        return TaskInfoVO.builder()
                .taskId(task.getId())
                .taskNo(task.getTaskNo())
                .taskName(task.getTaskName())
                .fileName(task.getFileName())
                .fileSize(task.getFileSize())
                .status(task.getStatus())
                .statusDisplay(TaskStatusEnum.getByType(task.getStatus()).getDescription())
                .createTime(task.getCreateTime())
                .creator(task.getCreator())
                .build();
    }
}