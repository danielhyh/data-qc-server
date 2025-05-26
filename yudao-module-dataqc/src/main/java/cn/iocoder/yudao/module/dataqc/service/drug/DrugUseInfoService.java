package cn.iocoder.yudao.module.dataqc.service.drug;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.DrugUseInfoPageReqVO;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.DrugUseInfoSaveReqVO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.drug.DrugUseInfoDO;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 药品使用情况 Service 接口
 *
 * @author 管理员
 */
public interface DrugUseInfoService {

    /**
     * 创建药品使用情况
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDrugUseInfo(@Valid DrugUseInfoSaveReqVO createReqVO);

    /**
     * 更新药品使用情况
     *
     * @param updateReqVO 更新信息
     */
    void updateDrugUseInfo(@Valid DrugUseInfoSaveReqVO updateReqVO);

    /**
     * 删除药品使用情况
     *
     * @param id 编号
     */
    void deleteDrugUseInfo(Long id);

    /**
    * 批量删除药品使用情况
    *
    * @param ids 编号
    */
    void deleteDrugUseInfoListByIds(List<Long> ids);

    /**
     * 获得药品使用情况
     *
     * @param id 编号
     * @return 药品使用情况
     */
    DrugUseInfoDO getDrugUseInfo(Long id);

    /**
     * 获得药品使用情况分页
     *
     * @param pageReqVO 分页查询
     * @return 药品使用情况分页
     */
    PageResult<DrugUseInfoDO> getDrugUseInfoPage(DrugUseInfoPageReqVO pageReqVO);
    /**
     * 查询使用情况列表
     */
    List<DrugUseInfoDO> selectUseList(DrugUseInfoPageReqVO queryVO);

    /**
     * 导入使用数据
     */
    String importUseData(MultipartFile file, boolean updateSupport) throws Exception;

    /**
     * 获取使用统计
     */
    List<Map<String, Object>> getUseStatistics(DrugUseInfoPageReqVO queryVO);

    /**
     * 获取科室用药排名
     */
    List<Map<String, Object>> getDepartmentRanking(String startDate, String endDate);

    /**
     * 获取药品使用排名
     */
    List<Map<String, Object>> getDrugUseRanking(String startDate, String endDate);
    /**
     * 基础药品分析
     */
    Map<String, Object> getBaseDrugAnalysis(String startDate, String endDate);

}