package cn.iocoder.yudao.module.drug.dal.mysql.batch;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.drug.controller.admin.batch.vo.ImportTaskDetailPageReqVO;
import cn.iocoder.yudao.module.drug.dal.dataobject.batch.ImportTaskDetailDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 药品数据导入任务明细 Mapper
 *
 * @author hyh
 */
@Mapper
public interface ImportTaskDetailMapper extends BaseMapperX<ImportTaskDetailDO> {

    default PageResult<ImportTaskDetailDO> selectPage(ImportTaskDetailPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ImportTaskDetailDO>()
                .eqIfPresent(ImportTaskDetailDO::getTaskId, reqVO.getTaskId())
                .eqIfPresent(ImportTaskDetailDO::getTaskNo, reqVO.getTaskNo())
                .eqIfPresent(ImportTaskDetailDO::getFileType, reqVO.getFileType())
                .likeIfPresent(ImportTaskDetailDO::getFileName, reqVO.getFileName())
                .eqIfPresent(ImportTaskDetailDO::getTargetTable, reqVO.getTargetTable())
                .eqIfPresent(ImportTaskDetailDO::getTableType, reqVO.getTableType())
                .eqIfPresent(ImportTaskDetailDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ImportTaskDetailDO::getParseStatus, reqVO.getParseStatus())
                .eqIfPresent(ImportTaskDetailDO::getImportStatus, reqVO.getImportStatus())
                .eqIfPresent(ImportTaskDetailDO::getQcStatus, reqVO.getQcStatus())
                .eqIfPresent(ImportTaskDetailDO::getTotalRows, reqVO.getTotalRows())
                .eqIfPresent(ImportTaskDetailDO::getValidRows, reqVO.getValidRows())
                .eqIfPresent(ImportTaskDetailDO::getSuccessRows, reqVO.getSuccessRows())
                .eqIfPresent(ImportTaskDetailDO::getFailedRows, reqVO.getFailedRows())
                .eqIfPresent(ImportTaskDetailDO::getQcPassedRows, reqVO.getQcPassedRows())
                .eqIfPresent(ImportTaskDetailDO::getQcFailedRows, reqVO.getQcFailedRows())
                .eqIfPresent(ImportTaskDetailDO::getProgressPercent, reqVO.getProgressPercent())
                .betweenIfPresent(ImportTaskDetailDO::getStartTime, reqVO.getStartTime())
                .betweenIfPresent(ImportTaskDetailDO::getParseEndTime, reqVO.getParseEndTime())
                .betweenIfPresent(ImportTaskDetailDO::getImportEndTime, reqVO.getImportEndTime())
                .betweenIfPresent(ImportTaskDetailDO::getQcEndTime, reqVO.getQcEndTime())
                .betweenIfPresent(ImportTaskDetailDO::getEndTime, reqVO.getEndTime())
                .eqIfPresent(ImportTaskDetailDO::getErrorMessage, reqVO.getErrorMessage())
                .eqIfPresent(ImportTaskDetailDO::getErrorRowsDetail, reqVO.getErrorRowsDetail())
                .eqIfPresent(ImportTaskDetailDO::getImportBatchNo, reqVO.getImportBatchNo())
                .eqIfPresent(ImportTaskDetailDO::getRetryCount, reqVO.getRetryCount())
                .eqIfPresent(ImportTaskDetailDO::getMaxRetryCount, reqVO.getMaxRetryCount())
                .betweenIfPresent(ImportTaskDetailDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ImportTaskDetailDO::getId));
    }

}