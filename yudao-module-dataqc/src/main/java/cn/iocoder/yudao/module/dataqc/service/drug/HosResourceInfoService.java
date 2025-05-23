package cn.iocoder.yudao.module.dataqc.service.drug;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.HosResourceInfoPageReqVO;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.HosResourceInfoSaveReqVO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.drug.HosResourceInfoDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 医疗机构资源信息 Service 接口
 *
 * @author 管理员
 */
public interface HosResourceInfoService {

    /**
     * 创建医疗机构资源信息
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createHosResourceInfo(@Valid HosResourceInfoSaveReqVO createReqVO);

    /**
     * 更新医疗机构资源信息
     *
     * @param updateReqVO 更新信息
     */
    void updateHosResourceInfo(@Valid HosResourceInfoSaveReqVO updateReqVO);

    /**
     * 删除医疗机构资源信息
     *
     * @param id 编号
     */
    void deleteHosResourceInfo(Long id);

    /**
    * 批量删除医疗机构资源信息
    *
    * @param ids 编号
    */
    void deleteHosResourceInfoListByIds(List<Long> ids);

    /**
     * 获得医疗机构资源信息
     *
     * @param id 编号
     * @return 医疗机构资源信息
     */
    HosResourceInfoDO getHosResourceInfo(Long id);

    /**
     * 获得医疗机构资源信息分页
     *
     * @param pageReqVO 分页查询
     * @return 医疗机构资源信息分页
     */
    PageResult<HosResourceInfoDO> getHosResourceInfoPage(HosResourceInfoPageReqVO pageReqVO);

}