package cn.iocoder.yudao.module.drug.service.batch;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.drug.controller.admin.batch.vo.ImportTaskDetailPageReqVO;
import cn.iocoder.yudao.module.drug.controller.admin.batch.vo.ImportTaskDetailSaveReqVO;
import cn.iocoder.yudao.module.drug.dal.dataobject.batch.ImportTaskDetailDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 药品数据导入任务明细 Service 接口
 *
 * @author hyh
 */
public interface ImportTaskDetailService {

    /**
     * 创建药品数据导入任务明细
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createImportTaskDetail(@Valid ImportTaskDetailSaveReqVO createReqVO);

    /**
     * 更新药品数据导入任务明细
     *
     * @param updateReqVO 更新信息
     */
    void updateImportTaskDetail(@Valid ImportTaskDetailSaveReqVO updateReqVO);

    /**
     * 删除药品数据导入任务明细
     *
     * @param id 编号
     */
    void deleteImportTaskDetail(Long id);

    /**
    * 批量删除药品数据导入任务明细
    *
    * @param ids 编号
    */
    void deleteImportTaskDetailListByIds(List<Long> ids);

    /**
     * 获得药品数据导入任务明细
     *
     * @param id 编号
     * @return 药品数据导入任务明细
     */
    ImportTaskDetailDO getImportTaskDetail(Long id);

    /**
     * 获得药品数据导入任务明细分页
     *
     * @param pageReqVO 分页查询
     * @return 药品数据导入任务明细分页
     */
    PageResult<ImportTaskDetailDO> getImportTaskDetailPage(ImportTaskDetailPageReqVO pageReqVO);

}