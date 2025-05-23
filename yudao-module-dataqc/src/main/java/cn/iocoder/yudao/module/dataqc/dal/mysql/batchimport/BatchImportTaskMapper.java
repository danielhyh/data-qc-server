package cn.iocoder.yudao.module.dataqc.dal.mysql.batchimport;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.dataqc.controller.admin.batchimport.vo.BatchImportTaskPageReqVO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.batchimport.BatchImportTaskDO;
import org.apache.ibatis.annotations.Mapper;

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

}