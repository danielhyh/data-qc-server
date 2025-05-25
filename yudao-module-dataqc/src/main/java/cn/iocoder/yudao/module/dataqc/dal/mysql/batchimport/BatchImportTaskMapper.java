package cn.iocoder.yudao.module.dataqc.dal.mysql.batchimport;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.dataqc.controller.admin.batchimport.vo.BatchImportTaskPageReqVO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.batchimport.BatchImportTaskDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 批量导入任务 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface BatchImportTaskMapper extends BaseMapperX<BatchImportTaskDO> {

    default PageResult<BatchImportTaskDO> selectPage(BatchImportTaskPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<BatchImportTaskDO>()
                .eqIfPresent(BatchImportTaskDO::getTaskNo, reqVO.getTaskNo())
                .likeIfPresent(BatchImportTaskDO::getTaskName, reqVO.getTaskName())
                .likeIfPresent(BatchImportTaskDO::getFileName, reqVO.getFileName())
                .eqIfPresent(BatchImportTaskDO::getFilePath, reqVO.getFilePath())
                .eqIfPresent(BatchImportTaskDO::getStatus, reqVO.getStatus())
                .eqIfPresent(BatchImportTaskDO::getTotalFiles, reqVO.getTotalFiles())
                .eqIfPresent(BatchImportTaskDO::getSuccessFiles, reqVO.getSuccessFiles())
                .eqIfPresent(BatchImportTaskDO::getFailFiles, reqVO.getFailFiles())
                .betweenIfPresent(BatchImportTaskDO::getStartTime, reqVO.getStartTime())
                .betweenIfPresent(BatchImportTaskDO::getEndTime, reqVO.getEndTime())
                .eqIfPresent(BatchImportTaskDO::getErrorMsg, reqVO.getErrorMsg())
                .eqIfPresent(BatchImportTaskDO::getResultDetail, reqVO.getResultDetail())
                .betweenIfPresent(BatchImportTaskDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(BatchImportTaskDO::getId));
    }
    /**
     * 查询指定状态的任务数量
     * 用于统计分析
     */
    Long selectCountByStatus(@Param("status") Integer status);

    /**
     * 查询指定时间范围内的任务统计
     * 支持按天、按月聚合统计
     */
    List<Map<String, Object>> selectTaskStatistics(@Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime,
                                                   @Param("groupBy") String groupBy);

    /**
     * 查询需要清理的过期任务
     * 用于定时清理功能
     */
    List<BatchImportTaskDO> selectExpiredTasks(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 查询正在进行的任务
     * 用于监控和限流
     */
    List<BatchImportTaskDO> selectProcessingTasks();

    /**
     * 批量更新任务状态
     * 用于批量操作
     */
    int updateStatusBatch(@Param("ids") List<Long> ids, @Param("status") Integer status);
}