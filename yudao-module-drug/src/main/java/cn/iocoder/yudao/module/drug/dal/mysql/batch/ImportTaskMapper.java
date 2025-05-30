package cn.iocoder.yudao.module.drug.dal.mysql.batch;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.drug.controller.admin.batch.vo.ImportTaskPageReqVO;
import cn.iocoder.yudao.module.drug.dal.dataobject.batch.ImportTaskDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 药品数据导入任务 Mapper
 *
 * @author hyh
 */
@Mapper
public interface ImportTaskMapper extends BaseMapperX<ImportTaskDO> {

    default PageResult<ImportTaskDO> selectPage(ImportTaskPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ImportTaskDO>()
                .eqIfPresent(ImportTaskDO::getTaskNo, reqVO.getTaskNo())
                .likeIfPresent(ImportTaskDO::getTaskName, reqVO.getTaskName())
                .eqIfPresent(ImportTaskDO::getImportType, reqVO.getImportType())
                .likeIfPresent(ImportTaskDO::getFileName, reqVO.getFileName())
                .eqIfPresent(ImportTaskDO::getFilePath, reqVO.getFilePath())
                .eqIfPresent(ImportTaskDO::getFileSize, reqVO.getFileSize())
                .eqIfPresent(ImportTaskDO::getExtractedFiles, reqVO.getExtractedFiles())
                .eqIfPresent(ImportTaskDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ImportTaskDO::getExtractStatus, reqVO.getExtractStatus())
                .eqIfPresent(ImportTaskDO::getImportStatus, reqVO.getImportStatus())
                .eqIfPresent(ImportTaskDO::getQcStatus, reqVO.getQcStatus())
                .eqIfPresent(ImportTaskDO::getTotalFiles, reqVO.getTotalFiles())
                .eqIfPresent(ImportTaskDO::getSuccessFiles, reqVO.getSuccessFiles())
                .eqIfPresent(ImportTaskDO::getFailedFiles, reqVO.getFailedFiles())
                .eqIfPresent(ImportTaskDO::getTotalRecords, reqVO.getTotalRecords())
                .eqIfPresent(ImportTaskDO::getSuccessRecords, reqVO.getSuccessRecords())
                .eqIfPresent(ImportTaskDO::getFailedRecords, reqVO.getFailedRecords())
                .eqIfPresent(ImportTaskDO::getProgressPercent, reqVO.getProgressPercent())
                .eqIfPresent(ImportTaskDO::getTableProgress, reqVO.getTableProgress())
                .betweenIfPresent(ImportTaskDO::getStartTime, reqVO.getStartTime())
                .betweenIfPresent(ImportTaskDO::getExtractEndTime, reqVO.getExtractEndTime())
                .betweenIfPresent(ImportTaskDO::getImportEndTime, reqVO.getImportEndTime())
                .betweenIfPresent(ImportTaskDO::getQcEndTime, reqVO.getQcEndTime())
                .betweenIfPresent(ImportTaskDO::getEndTime, reqVO.getEndTime())
                .eqIfPresent(ImportTaskDO::getErrorMessage, reqVO.getErrorMessage())
                .eqIfPresent(ImportTaskDO::getErrorDetail, reqVO.getErrorDetail())
                .betweenIfPresent(ImportTaskDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ImportTaskDO::getId));
    }

}