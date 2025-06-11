package cn.iocoder.yudao.module.drug.service.rule;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.drug.controller.admin.rule.vo.QcRulePageReqVO;
import cn.iocoder.yudao.module.drug.controller.admin.rule.vo.QcRuleSaveReqVO;
import cn.iocoder.yudao.module.drug.dal.dataobject.rule.QcRuleDO;
import cn.iocoder.yudao.module.drug.dal.mysql.rule.QcRuleMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.drug.enums.DrugErrorCodeConstants.QC_RULE_NOT_FOUND;

/**
 * 质控规则 Service 实现类
 *
 * @author hyh
 */
@Service
@Validated
public class QcRuleServiceImpl implements QcRuleService {

    @Resource
    private QcRuleMapper qcRuleMapper;

    @Override
    public Long createQcRule(QcRuleSaveReqVO createReqVO) {
        // 插入
        QcRuleDO qcRule = BeanUtils.toBean(createReqVO, QcRuleDO.class);
        qcRuleMapper.insert(qcRule);
        // 返回
        return qcRule.getId();
    }

    @Override
    public void updateQcRule(QcRuleSaveReqVO updateReqVO) {
        // 校验存在
        validateQcRuleExists(updateReqVO.getId());
        // 更新
        QcRuleDO updateObj = BeanUtils.toBean(updateReqVO, QcRuleDO.class);
        qcRuleMapper.updateById(updateObj);
    }

    @Override
    public void deleteQcRule(Long id) {
        // 校验存在
        validateQcRuleExists(id);
        // 删除
        qcRuleMapper.deleteById(id);
    }

    @Override
        public void deleteQcRuleListByIds(List<Long> ids) {
        // 校验存在
        validateQcRuleExists(ids);
        // 删除
        qcRuleMapper.deleteByIds(ids);
        }

    private void validateQcRuleExists(List<Long> ids) {
        List<QcRuleDO> list = qcRuleMapper.selectByIds(ids);
        if (CollUtil.isEmpty(list) || list.size() != ids.size()) {
            throw exception(QC_RULE_NOT_FOUND);
        }
    }

    private void validateQcRuleExists(Long id) {
        if (qcRuleMapper.selectById(id) == null) {
            throw exception(QC_RULE_NOT_FOUND);
        }
    }

    @Override
    public QcRuleDO getQcRule(Long id) {
        return qcRuleMapper.selectById(id);
    }

    @Override
    public PageResult<QcRuleDO> getQcRulePage(QcRulePageReqVO pageReqVO) {
        return qcRuleMapper.selectPage(pageReqVO);
    }

}