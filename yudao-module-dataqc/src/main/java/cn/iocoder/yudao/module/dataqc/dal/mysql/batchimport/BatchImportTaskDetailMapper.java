package cn.iocoder.yudao.module.dataqc.dal.mysql.batchimport;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.dataqc.controller.admin.batchimport.vo.BatchImportTaskDetailPageReqVO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.batchimport.BatchImportTaskDetailDO;
import org.apache.ibatis.annotations.Mapper;

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

}