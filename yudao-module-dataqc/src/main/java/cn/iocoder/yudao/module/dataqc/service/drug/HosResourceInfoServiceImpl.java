package cn.iocoder.yudao.module.dataqc.service.drug;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.HosResourceInfoPageReqVO;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.HosResourceInfoSaveReqVO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.drug.HosResourceInfoDO;
import cn.iocoder.yudao.module.dataqc.dal.mysql.drug.HosResourceInfoMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.dataqc.enums.ErrorCodeConstants.HOS_RESOURCE_INFO_NOT_EXISTS;

/**
 * 医疗机构资源信息 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class HosResourceInfoServiceImpl implements HosResourceInfoService {

    @Resource
    private HosResourceInfoMapper hosResourceInfoMapper;

    @Override
    public Long createHosResourceInfo(HosResourceInfoSaveReqVO createReqVO) {
        // 插入
        HosResourceInfoDO hosResourceInfo = BeanUtils.toBean(createReqVO, HosResourceInfoDO.class);
        hosResourceInfoMapper.insert(hosResourceInfo);
        // 返回
        return hosResourceInfo.getId();
    }

    @Override
    public void updateHosResourceInfo(HosResourceInfoSaveReqVO updateReqVO) {
        // 校验存在
        validateHosResourceInfoExists(updateReqVO.getId());
        // 更新
        HosResourceInfoDO updateObj = BeanUtils.toBean(updateReqVO, HosResourceInfoDO.class);
        hosResourceInfoMapper.updateById(updateObj);
    }

    @Override
    public void deleteHosResourceInfo(Long id) {
        // 校验存在
        validateHosResourceInfoExists(id);
        // 删除
        hosResourceInfoMapper.deleteById(id);
    }

    @Override
    public void deleteHosResourceInfoListByIds(List<Long> ids) {
        // 校验存在
        validateHosResourceInfoExists(ids);
        // 删除
        hosResourceInfoMapper.deleteByIds(ids);
    }

    private void validateHosResourceInfoExists(List<Long> ids) {
        List<HosResourceInfoDO> list = hosResourceInfoMapper.selectByIds(ids);
        if (CollUtil.isEmpty(list) || list.size() != ids.size()) {
            throw exception(HOS_RESOURCE_INFO_NOT_EXISTS);
        }
    }

    private void validateHosResourceInfoExists(Long id) {
        if (hosResourceInfoMapper.selectById(id) == null) {
            throw exception(HOS_RESOURCE_INFO_NOT_EXISTS);
        }
    }

    @Override
    public HosResourceInfoDO getHosResourceInfo(Long id) {
        return hosResourceInfoMapper.selectById(id);
    }

    @Override
    public PageResult<HosResourceInfoDO> getHosResourceInfoPage(HosResourceInfoPageReqVO pageReqVO) {
        return hosResourceInfoMapper.selectPage(pageReqVO);
    }

}