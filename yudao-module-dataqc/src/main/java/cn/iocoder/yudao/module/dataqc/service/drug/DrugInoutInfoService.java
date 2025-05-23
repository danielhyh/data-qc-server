package cn.iocoder.yudao.module.dataqc.service.drug;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.DrugInoutInfoPageReqVO;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.DrugInoutInfoSaveReqVO;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.InoutStatVO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.drug.DrugInoutInfoDO;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 药品出入库 Service 接口
 *
 * @author 管理员
 */
public interface DrugInoutInfoService {

    /**
     * 创建药品出入库
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDrugInoutInfo(@Valid DrugInoutInfoSaveReqVO createReqVO);

    /**
     * 更新药品出入库
     *
     * @param updateReqVO 更新信息
     */
    void updateDrugInoutInfo(@Valid DrugInoutInfoSaveReqVO updateReqVO);

    /**
     * 删除药品出入库
     *
     * @param id 编号
     */
    void deleteDrugInoutInfo(Long id);

    /**
    * 批量删除药品出入库
    *
    * @param ids 编号
    */
    void deleteDrugInoutInfoListByIds(List<Long> ids);

    /**
     * 获得药品出入库
     *
     * @param id 编号
     * @return 药品出入库
     */
    DrugInoutInfoDO getDrugInoutInfo(Long id);

    /**
     * 获得药品出入库分页
     *
     * @param pageReqVO 分页查询
     * @return 药品出入库分页
     */
    PageResult<DrugInoutInfoDO> getDrugInoutInfoPage(DrugInoutInfoPageReqVO pageReqVO);

    /**
     * 查询出入库列表
     */
    List<DrugInoutInfoDO> selectInoutList(DrugInoutInfoPageReqVO queryVO);

    /**
     * 导入入库数据
     */
    String importInData(MultipartFile file, boolean updateSupport) throws Exception;

    /**
     * 导入出库数据
     */
    String importOutData(MultipartFile file, boolean updateSupport) throws Exception;

    /**
     * 获取库存汇总
     */
    List<Map<String, Object>> getStockSummary(DrugInoutInfoPageReqVO queryVO);

    /**
     * 获取出入库统计
     */
    InoutStatVO getInoutStatistics(String startDate, String endDate);

}