package cn.iocoder.yudao.module.dataqc.dal.mysql.batchimport;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.dataqc.controller.admin.batchimport.vo.BatchImportTaskDetailPageReqVO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.batchimport.BatchImportTaskDetailDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 批量导入任务明细 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface BatchImportTaskDetailMapper extends BaseMapperX<BatchImportTaskDetailDO> {

    default PageResult<BatchImportTaskDetailDO> selectPage(BatchImportTaskDetailPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<BatchImportTaskDetailDO>()
                .eqIfPresent(BatchImportTaskDetailDO::getTaskId, reqVO.getTaskId())
                .eqIfPresent(BatchImportTaskDetailDO::getTaskNo, reqVO.getTaskNo())
                .eqIfPresent(BatchImportTaskDetailDO::getFileType, reqVO.getFileType())
                .likeIfPresent(BatchImportTaskDetailDO::getFileName, reqVO.getFileName())
                .likeIfPresent(BatchImportTaskDetailDO::getTableName, reqVO.getTableName())
                .eqIfPresent(BatchImportTaskDetailDO::getStatus, reqVO.getStatus())
                .eqIfPresent(BatchImportTaskDetailDO::getTotalRows, reqVO.getTotalRows())
                .eqIfPresent(BatchImportTaskDetailDO::getSuccessRows, reqVO.getSuccessRows())
                .eqIfPresent(BatchImportTaskDetailDO::getFailRows, reqVO.getFailRows())
                .betweenIfPresent(BatchImportTaskDetailDO::getStartTime, reqVO.getStartTime())
                .betweenIfPresent(BatchImportTaskDetailDO::getEndTime, reqVO.getEndTime())
                .eqIfPresent(BatchImportTaskDetailDO::getErrorMsg, reqVO.getErrorMsg())
                .eqIfPresent(BatchImportTaskDetailDO::getErrorDetail, reqVO.getErrorDetail())
                .eqIfPresent(BatchImportTaskDetailDO::getImportBatchNo, reqVO.getImportBatchNo())
                .betweenIfPresent(BatchImportTaskDetailDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(BatchImportTaskDetailDO::getId));
    }
    /**
     * 根据任务ID查询明细列表
     */
    List<BatchImportTaskDetailDO> selectByTaskId(@Param("taskId") Long taskId);

    /**
     * 查询指定任务的失败明细
     */
    List<BatchImportTaskDetailDO> selectFailedDetailsByTaskId(@Param("taskId") Long taskId);

    /**
     * 统计指定任务的处理情况
     */
    Map<String, Object> selectTaskProcessingSummary(@Param("taskId") Long taskId);

    /**
     * 批量更新明细状态
     */
    int updateStatusByTaskIdAndFileType(@Param("taskId") Long taskId,
                                        @Param("fileType") String fileType,
                                        @Param("status") Integer status);

    /**
     * 查询文件类型处理统计
     * 用于分析哪种文件类型最容易出错
     */
    List<Map<String, Object>> selectFileTypeStatistics(@Param("days") Integer days);

}