package cn.iocoder.yudao.module.drug.service.rule;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.drug.controller.admin.rule.vo.QcRulePageReqVO;
import cn.iocoder.yudao.module.drug.controller.admin.rule.vo.QcRuleSaveReqVO;
import cn.iocoder.yudao.module.drug.dal.dataobject.rule.QcRuleDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 质控规则 Service 接口
 *
 * @author hyh
 */
public interface QcRuleService {

    /**
     * 创建质控规则
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createQcRule(@Valid QcRuleSaveReqVO createReqVO);

    /**
     * 更新质控规则
     *
     * @param updateReqVO 更新信息
     */
    void updateQcRule(@Valid QcRuleSaveReqVO updateReqVO);

    /**
     * 删除质控规则
     *
     * @param id 编号
     */
    void deleteQcRule(Long id);

    /**
    * 批量删除质控规则
    *
    * @param ids 编号
    */
    void deleteQcRuleListByIds(List<Long> ids);

    /**
     * 获得质控规则
     *
     * @param id 编号
     * @return 质控规则
     */
    QcRuleDO getQcRule(Long id);

    /**
     * 获得质控规则分页
     *
     * @param pageReqVO 分页查询
     * @return 质控规则分页
     */
    PageResult<QcRuleDO> getQcRulePage(QcRulePageReqVO pageReqVO);

}