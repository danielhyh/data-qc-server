package cn.iocoder.yudao.module.dataqc.dal.mysql.drug;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.DrugListPageReqVO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.drug.DrugListDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 药品目录 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface DrugListMapper extends BaseMapperX<DrugListDO> {

    default PageResult<DrugListDO> selectPage(DrugListPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DrugListDO>()
                .eqIfPresent(DrugListDO::getSerialNum, reqVO.getSerialNum())
                .eqIfPresent(DrugListDO::getDomainCode, reqVO.getDomainCode())
                .eqIfPresent(DrugListDO::getOrganizationCode, reqVO.getOrganizationCode())
                .likeIfPresent(DrugListDO::getOrganizationName, reqVO.getOrganizationName())
                .eqIfPresent(DrugListDO::getHospitalCode, reqVO.getHospitalCode())
                .betweenIfPresent(DrugListDO::getUploadDate, reqVO.getUploadDate())
                .eqIfPresent(DrugListDO::getYpid, reqVO.getYpid())
                .eqIfPresent(DrugListDO::getPrDrugId, reqVO.getPrDrugId())
                .eqIfPresent(DrugListDO::getHosDrugId, reqVO.getHosDrugId())
                .eqIfPresent(DrugListDO::getApprovalNum, reqVO.getApprovalNum())
                .likeIfPresent(DrugListDO::getDrugName, reqVO.getDrugName())
                .likeIfPresent(DrugListDO::getProductName, reqVO.getProductName())
                .likeIfPresent(DrugListDO::getTradeName, reqVO.getTradeName())
                .likeIfPresent(DrugListDO::getTradeEngName, reqVO.getTradeEngName())
                .eqIfPresent(DrugListDO::getManufacturer, reqVO.getManufacturer())
                .eqIfPresent(DrugListDO::getDrugForm, reqVO.getDrugForm())
                .eqIfPresent(DrugListDO::getDrugSpec, reqVO.getDrugSpec())
                .eqIfPresent(DrugListDO::getDosageUnit, reqVO.getDosageUnit())
                .eqIfPresent(DrugListDO::getPackUnit, reqVO.getPackUnit())
                .eqIfPresent(DrugListDO::getDrugFactor, reqVO.getDrugFactor())
                .eqIfPresent(DrugListDO::getUnityPurchaseFlag, reqVO.getUnityPurchaseFlag())
                .eqIfPresent(DrugListDO::getBaseFlag, reqVO.getBaseFlag())
                .eqIfPresent(DrugListDO::getUniformityFlag, reqVO.getUniformityFlag())
                .eqIfPresent(DrugListDO::getImportBatchNo, reqVO.getImportBatchNo())
                .betweenIfPresent(DrugListDO::getImportTime, reqVO.getImportTime())
                .betweenIfPresent(DrugListDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(DrugListDO::getId));
    }

    /**
     * 根据YPID批量查询药品信息
     */
    List<DrugListDO> selectByYpidList(@Param("ypidList") List<String> ypidList);

    /**
     * 统计药品分类信息
     */
    List<Map<String, Object>> selectDrugCategoryStats();

    /**
     * 获取药品总数统计
     */
    Map<String, Object> selectDrugTotalStats();

}