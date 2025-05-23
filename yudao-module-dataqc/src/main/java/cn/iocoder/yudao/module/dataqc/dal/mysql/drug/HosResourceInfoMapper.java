package cn.iocoder.yudao.module.dataqc.dal.mysql.drug;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.HosResourceInfoPageReqVO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.drug.HosResourceInfoDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 医疗机构资源信息 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface HosResourceInfoMapper extends BaseMapperX<HosResourceInfoDO> {

    default PageResult<HosResourceInfoDO> selectPage(HosResourceInfoPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<HosResourceInfoDO>()
                .eqIfPresent(HosResourceInfoDO::getDomainCode, reqVO.getDomainCode())
                .eqIfPresent(HosResourceInfoDO::getOrganizationCode, reqVO.getOrganizationCode())
                .likeIfPresent(HosResourceInfoDO::getOrganizationName, reqVO.getOrganizationName())
                .eqIfPresent(HosResourceInfoDO::getHospitalCode, reqVO.getHospitalCode())
                .betweenIfPresent(HosResourceInfoDO::getStatDate, reqVO.getStatDate())
                .eqIfPresent(HosResourceInfoDO::getBedsNum, reqVO.getBedsNum())
                .eqIfPresent(HosResourceInfoDO::getPracDockerNum, reqVO.getPracDockerNum())
                .eqIfPresent(HosResourceInfoDO::getAssDockerNum, reqVO.getAssDockerNum())
                .eqIfPresent(HosResourceInfoDO::getVisitCount, reqVO.getVisitCount())
                .eqIfPresent(HosResourceInfoDO::getLeaveHosCount, reqVO.getLeaveHosCount())
                .eqIfPresent(HosResourceInfoDO::getDrugSellAmount, reqVO.getDrugSellAmount())
                .eqIfPresent(HosResourceInfoDO::getYpPurchaseAmount, reqVO.getYpPurchaseAmount())
                .eqIfPresent(HosResourceInfoDO::getYpSellAmount, reqVO.getYpSellAmount())
                .eqIfPresent(HosResourceInfoDO::getKlPurchaseAmount, reqVO.getKlPurchaseAmount())
                .eqIfPresent(HosResourceInfoDO::getKlSellAmount, reqVO.getKlSellAmount())
                .betweenIfPresent(HosResourceInfoDO::getUploadDate, reqVO.getUploadDate())
                .eqIfPresent(HosResourceInfoDO::getImportBatchNo, reqVO.getImportBatchNo())
                .betweenIfPresent(HosResourceInfoDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(HosResourceInfoDO::getId));
    }
    /**
     * 查询最新季度数据
     */
    HosResourceInfoDO selectLatestByHospital(@Param("hospitalCode") String hospitalCode);

    /**
     * 查询资源趋势
     */
    List<Map<String, Object>> selectResourceTrend(@Param("hospitalCode") String hospitalCode,
                                                  @Param("year") String year);

    /**
     * 获取机构对比数据
     */
    List<Map<String, Object>> selectHospitalComparison(@Param("statDate") String statDate);

}