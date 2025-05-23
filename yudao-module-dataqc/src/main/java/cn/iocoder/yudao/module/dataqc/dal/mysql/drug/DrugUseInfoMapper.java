package cn.iocoder.yudao.module.dataqc.dal.mysql.drug;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.DrugUseInfoPageReqVO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.drug.DrugUseInfoDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 药品使用情况 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface DrugUseInfoMapper extends BaseMapperX<DrugUseInfoDO> {

    default PageResult<DrugUseInfoDO> selectPage(DrugUseInfoPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DrugUseInfoDO>()
                .eqIfPresent(DrugUseInfoDO::getSerialNum, reqVO.getSerialNum())
                .eqIfPresent(DrugUseInfoDO::getDomainCode, reqVO.getDomainCode())
                .eqIfPresent(DrugUseInfoDO::getOrganizationCode, reqVO.getOrganizationCode())
                .likeIfPresent(DrugUseInfoDO::getOrganizationName, reqVO.getOrganizationName())
                .eqIfPresent(DrugUseInfoDO::getHospitalCode, reqVO.getHospitalCode())
                .betweenIfPresent(DrugUseInfoDO::getUploadDate, reqVO.getUploadDate())
                .eqIfPresent(DrugUseInfoDO::getYpid, reqVO.getYpid())
                .eqIfPresent(DrugUseInfoDO::getPrDrugId, reqVO.getPrDrugId())
                .eqIfPresent(DrugUseInfoDO::getHosDrugId, reqVO.getHosDrugId())
                .likeIfPresent(DrugUseInfoDO::getProductName, reqVO.getProductName())
                .betweenIfPresent(DrugUseInfoDO::getSellDate, reqVO.getSellDate())
                .eqIfPresent(DrugUseInfoDO::getSellPackPrice, reqVO.getSellPackPrice())
                .eqIfPresent(DrugUseInfoDO::getSellPackQuantity, reqVO.getSellPackQuantity())
                .eqIfPresent(DrugUseInfoDO::getSellDosagePrice, reqVO.getSellDosagePrice())
                .eqIfPresent(DrugUseInfoDO::getSellDosageQuantity, reqVO.getSellDosageQuantity())
                .eqIfPresent(DrugUseInfoDO::getDepartmentCode, reqVO.getDepartmentCode())
                .likeIfPresent(DrugUseInfoDO::getDepartmentName, reqVO.getDepartmentName())
                .eqIfPresent(DrugUseInfoDO::getDoctorCode, reqVO.getDoctorCode())
                .likeIfPresent(DrugUseInfoDO::getDoctorName, reqVO.getDoctorName())
                .eqIfPresent(DrugUseInfoDO::getPatientType, reqVO.getPatientType())
                .eqIfPresent(DrugUseInfoDO::getImportBatchNo, reqVO.getImportBatchNo())
                .betweenIfPresent(DrugUseInfoDO::getImportTime, reqVO.getImportTime())
                .betweenIfPresent(DrugUseInfoDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(DrugUseInfoDO::getId));
    }
    /**
     * 获取使用统计
     */
    List<Map<String, Object>> selectUseStatistics(DrugUseInfoPageReqVO queryVO);

    /**
     * 获取科室用药排名
     */
    List<Map<String, Object>> selectDepartmentRanking(@Param("startDate") String startDate,
                                                      @Param("endDate") String endDate);

    /**
     * 获取药品使用排名
     */
    List<Map<String, Object>> selectDrugUseRanking(@Param("startDate") String startDate,
                                                   @Param("endDate") String endDate,
                                                   @Param("limit") Integer limit);

    /**
     * 基药使用分析
     */
    Map<String, Object> selectBaseDrugAnalysis(@Param("startDate") String startDate,
                                               @Param("endDate") String endDate);

    /**
     * 按患者类型统计
     */
    List<Map<String, Object>> selectPatientTypeStats(@Param("startDate") String startDate,
                                                     @Param("endDate") String endDate);

}