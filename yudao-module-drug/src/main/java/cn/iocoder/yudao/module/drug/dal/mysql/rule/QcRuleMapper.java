package cn.iocoder.yudao.module.drug.dal.mysql.rule;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.drug.controller.admin.rule.vo.QcRulePageReqVO;
import cn.iocoder.yudao.module.drug.dal.dataobject.rule.QcRuleDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 质控规则 Mapper
 *
 * @author hyh
 */
@Mapper
public interface QcRuleMapper extends BaseMapperX<QcRuleDO> {

    default PageResult<QcRuleDO> selectPage(QcRulePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<QcRuleDO>()
                .eqIfPresent(QcRuleDO::getRuleCode, reqVO.getRuleCode())
                .likeIfPresent(QcRuleDO::getRuleName, reqVO.getRuleName())
                .eqIfPresent(QcRuleDO::getRuleType, reqVO.getRuleType())
                .eqIfPresent(QcRuleDO::getRuleCategory, reqVO.getRuleCategory())
                .eqIfPresent(QcRuleDO::getTableType, reqVO.getTableType())
                .likeIfPresent(QcRuleDO::getFieldName, reqVO.getFieldName())
                .eqIfPresent(QcRuleDO::getRuleExpression, reqVO.getRuleExpression())
                .eqIfPresent(QcRuleDO::getErrorMessage, reqVO.getErrorMessage())
                .eqIfPresent(QcRuleDO::getErrorLevel, reqVO.getErrorLevel())
                .eqIfPresent(QcRuleDO::getThresholdValue, reqVO.getThresholdValue())
                .eqIfPresent(QcRuleDO::getPriority, reqVO.getPriority())
                .eqIfPresent(QcRuleDO::getEnabled, reqVO.getEnabled())
                .eqIfPresent(QcRuleDO::getDescription, reqVO.getDescription())
                .betweenIfPresent(QcRuleDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(QcRuleDO::getId));
    }

}