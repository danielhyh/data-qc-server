package cn.iocoder.yudao.module.dataqc.service.drug;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.DrugUseInfoPageReqVO;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.DrugUseInfoSaveReqVO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.drug.DrugUseInfoDO;
import cn.iocoder.yudao.module.dataqc.dal.mysql.drug.DrugUseInfoMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.dataqc.enums.ErrorCodeConstants.DRUG_USE_INFO_NOT_EXISTS;

/**
 * 药品使用情况 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class DrugUseInfoServiceImpl implements DrugUseInfoService {

    @Resource
    private DrugUseInfoMapper drugUseInfoMapper;

    @Override
    public Long createDrugUseInfo(DrugUseInfoSaveReqVO createReqVO) {
        // 插入
        DrugUseInfoDO drugUseInfo = BeanUtils.toBean(createReqVO, DrugUseInfoDO.class);
        drugUseInfoMapper.insert(drugUseInfo);
        // 返回
        return drugUseInfo.getId();
    }

    @Override
    public void updateDrugUseInfo(DrugUseInfoSaveReqVO updateReqVO) {
        // 校验存在
        validateDrugUseInfoExists(updateReqVO.getId());
        // 更新
        DrugUseInfoDO updateObj = BeanUtils.toBean(updateReqVO, DrugUseInfoDO.class);
        drugUseInfoMapper.updateById(updateObj);
    }

    @Override
    public void deleteDrugUseInfo(Long id) {
        // 校验存在
        validateDrugUseInfoExists(id);
        // 删除
        drugUseInfoMapper.deleteById(id);
    }

    @Override
        public void deleteDrugUseInfoListByIds(List<Long> ids) {
        // 校验存在
        validateDrugUseInfoExists(ids);
        // 删除
        drugUseInfoMapper.deleteByIds(ids);
        }

    private void validateDrugUseInfoExists(List<Long> ids) {
        List<DrugUseInfoDO> list = drugUseInfoMapper.selectByIds(ids);
        if (CollUtil.isEmpty(list) || list.size() != ids.size()) {
            throw exception(DRUG_USE_INFO_NOT_EXISTS);
        }
    }

    private void validateDrugUseInfoExists(Long id) {
        if (drugUseInfoMapper.selectById(id) == null) {
            throw exception(DRUG_USE_INFO_NOT_EXISTS);
        }
    }

    @Override
    public DrugUseInfoDO getDrugUseInfo(Long id) {
        return drugUseInfoMapper.selectById(id);
    }

    @Override
    public PageResult<DrugUseInfoDO> getDrugUseInfoPage(DrugUseInfoPageReqVO pageReqVO) {
        return drugUseInfoMapper.selectPage(pageReqVO);
    }

}