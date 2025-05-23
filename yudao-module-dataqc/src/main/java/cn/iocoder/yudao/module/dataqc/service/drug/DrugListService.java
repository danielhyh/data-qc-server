package cn.iocoder.yudao.module.dataqc.service.drug;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.DrugListPageReqVO;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.DrugListSaveReqVO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.drug.DrugListDO;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 药品目录 Service 接口
 *
 * @author 管理员
 */
public interface DrugListService {

    /**
     * 创建药品目录
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDrugList(@Valid DrugListSaveReqVO createReqVO);

    /**
     * 更新药品目录
     *
     * @param updateReqVO 更新信息
     */
    void updateDrugList(@Valid DrugListSaveReqVO updateReqVO);

    /**
     * 删除药品目录
     *
     * @param id 编号
     */
    void deleteDrugList(Long id);

    /**
    * 批量删除药品目录
    *
    * @param ids 编号
    */
    void deleteDrugListListByIds(List<Long> ids);

    /**
     * 获得药品目录
     *
     * @param id 编号
     * @return 药品目录
     */
    DrugListDO getDrugList(Long id);

    /**
     * 获得药品目录分页
     *
     * @param pageReqVO 分页查询
     * @return 药品目录分页
     */
    PageResult<DrugListDO> getDrugListPage(DrugListPageReqVO pageReqVO);
    /**
     * 查询药品目录列表
     */
    List<DrugListDO> selectDrugList(DrugListPageReqVO queryVO);

    /**
     * 导入药品目录数据
     * @param file Excel文件
     * @param updateSupport 是否更新已存在数据
     * @return 导入结果
     */
    String importDrugList(MultipartFile file, boolean updateSupport) throws Exception;

    /**
     * 根据院内编码查询药品
     */
    DrugListDO selectByHosDrugId(String hosDrugId);

    /**
     * 校验药品编码是否存在
     */
    boolean checkHosDrugIdExist(String hosDrugId, Long excludeId);

}