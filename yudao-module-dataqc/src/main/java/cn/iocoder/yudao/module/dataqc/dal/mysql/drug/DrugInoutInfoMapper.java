package cn.iocoder.yudao.module.dataqc.dal.mysql.drug;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.DrugInoutInfoPageReqVO;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.InoutStatVO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.drug.DrugInoutInfoDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 药品出入库 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface DrugInoutInfoMapper extends BaseMapperX<DrugInoutInfoDO> {

    default PageResult<DrugInoutInfoDO> selectPage(DrugInoutInfoPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DrugInoutInfoDO>()
                .eqIfPresent(DrugInoutInfoDO::getSerialNum, reqVO.getSerialNum())
                .eqIfPresent(DrugInoutInfoDO::getDomainCode, reqVO.getDomainCode())
                .eqIfPresent(DrugInoutInfoDO::getOrganizationCode, reqVO.getOrganizationCode())
                .likeIfPresent(DrugInoutInfoDO::getOrganizationName, reqVO.getOrganizationName())
                .eqIfPresent(DrugInoutInfoDO::getHospitalCode, reqVO.getHospitalCode())
                .betweenIfPresent(DrugInoutInfoDO::getUploadDate, reqVO.getUploadDate())
                .eqIfPresent(DrugInoutInfoDO::getYpid, reqVO.getYpid())
                .eqIfPresent(DrugInoutInfoDO::getPrDrugId, reqVO.getPrDrugId())
                .eqIfPresent(DrugInoutInfoDO::getHosDrugId, reqVO.getHosDrugId())
                .likeIfPresent(DrugInoutInfoDO::getProductName, reqVO.getProductName())
                .betweenIfPresent(DrugInoutInfoDO::getOutInDate, reqVO.getOutInDate())
                .eqIfPresent(DrugInoutInfoDO::getIoType, reqVO.getIoType())
                .eqIfPresent(DrugInoutInfoDO::getInPackQuantity, reqVO.getInPackQuantity())
                .eqIfPresent(DrugInoutInfoDO::getInDosageQuantity, reqVO.getInDosageQuantity())
                .eqIfPresent(DrugInoutInfoDO::getInPackPrice, reqVO.getInPackPrice())
                .eqIfPresent(DrugInoutInfoDO::getInDosagePrice, reqVO.getInDosagePrice())
                .eqIfPresent(DrugInoutInfoDO::getOutPackQuantity, reqVO.getOutPackQuantity())
                .eqIfPresent(DrugInoutInfoDO::getOutDosageQuantity, reqVO.getOutDosageQuantity())
                .eqIfPresent(DrugInoutInfoDO::getSupplierCode, reqVO.getSupplierCode())
                .likeIfPresent(DrugInoutInfoDO::getSupplierName, reqVO.getSupplierName())
                .eqIfPresent(DrugInoutInfoDO::getBatchNo, reqVO.getBatchNo())
                .betweenIfPresent(DrugInoutInfoDO::getProductionDate, reqVO.getProductionDate())
                .betweenIfPresent(DrugInoutInfoDO::getExpiryDate, reqVO.getExpiryDate())
                .eqIfPresent(DrugInoutInfoDO::getImportBatchNo, reqVO.getImportBatchNo())
                .betweenIfPresent(DrugInoutInfoDO::getImportTime, reqVO.getImportTime())
                .eqIfPresent(DrugInoutInfoDO::getSourceType, reqVO.getSourceType())
                .betweenIfPresent(DrugInoutInfoDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(DrugInoutInfoDO::getId));
    }
    /**
     * 查询库存汇总
     */
    List<Map<String, Object>> selectStockSummary(DrugInoutInfoPageReqVO queryVO);

    /**
     * 查询出入库统计
     */
    InoutStatVO selectInoutStatistics(@Param("startDate") String startDate,
                                      @Param("endDate") String endDate);

    /**
     * 按月统计出入库金额
     */
    List<Map<String, Object>> selectMonthlyInoutAmount(@Param("year") String year);

    /**
     * 查询即将过期的药品
     */
    List<Map<String, Object>> selectExpiryWarning(@Param("days") Integer days);

    /**
     * 供应商采购统计
     */
    List<Map<String, Object>> selectSupplierStats(@Param("startDate") String startDate,
                                                  @Param("endDate") String endDate);

}