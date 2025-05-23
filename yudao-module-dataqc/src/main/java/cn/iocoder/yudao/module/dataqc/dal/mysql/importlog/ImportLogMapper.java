package cn.iocoder.yudao.module.dataqc.dal.mysql.importlog;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.dataqc.controller.admin.importlog.vo.ImportLogPageReqVO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.importlog.ImportLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据导入日志 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface ImportLogMapper extends BaseMapperX<ImportLogDO> {

    default PageResult<ImportLogDO> selectPage(ImportLogPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ImportLogDO>()
                .eqIfPresent(ImportLogDO::getBatchNo, reqVO.getBatchNo())
                .likeIfPresent(ImportLogDO::getFileName, reqVO.getFileName())
                .eqIfPresent(ImportLogDO::getFileType, reqVO.getFileType())
                .likeIfPresent(ImportLogDO::getTableName, reqVO.getTableName())
                .eqIfPresent(ImportLogDO::getTotalRows, reqVO.getTotalRows())
                .eqIfPresent(ImportLogDO::getSuccessRows, reqVO.getSuccessRows())
                .eqIfPresent(ImportLogDO::getFailRows, reqVO.getFailRows())
                .eqIfPresent(ImportLogDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ImportLogDO::getErrorMsg, reqVO.getErrorMsg())
                .betweenIfPresent(ImportLogDO::getStartTime, reqVO.getStartTime())
                .betweenIfPresent(ImportLogDO::getEndTime, reqVO.getEndTime())
                .eqIfPresent(ImportLogDO::getOperatorId, reqVO.getOperatorId())
                .likeIfPresent(ImportLogDO::getOperatorName, reqVO.getOperatorName())
                .betweenIfPresent(ImportLogDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ImportLogDO::getId));
    }

}