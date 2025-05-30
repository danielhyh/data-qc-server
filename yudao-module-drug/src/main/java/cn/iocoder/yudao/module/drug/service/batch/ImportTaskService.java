package cn.iocoder.yudao.module.drug.service.batch;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.drug.controller.admin.batch.vo.ImportTaskPageReqVO;
import cn.iocoder.yudao.module.drug.controller.admin.batch.vo.ImportTaskSaveReqVO;
import cn.iocoder.yudao.module.drug.dal.dataobject.batch.ImportTaskDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 药品数据导入任务 Service 接口
 *
 * @author hyh
 */
public interface ImportTaskService {

    /**
     * 创建药品数据导入任务
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createImportTask(@Valid ImportTaskSaveReqVO createReqVO);

    /**
     * 更新药品数据导入任务
     *
     * @param updateReqVO 更新信息
     */
    void updateImportTask(@Valid ImportTaskSaveReqVO updateReqVO);

    /**
     * 删除药品数据导入任务
     *
     * @param id 编号
     */
    void deleteImportTask(Long id);

    /**
    * 批量删除药品数据导入任务
    *
    * @param ids 编号
    */
    void deleteImportTaskListByIds(List<Long> ids);

    /**
     * 获得药品数据导入任务
     *
     * @param id 编号
     * @return 药品数据导入任务
     */
    ImportTaskDO getImportTask(Long id);

    /**
     * 获得药品数据导入任务分页
     *
     * @param pageReqVO 分页查询
     * @return 药品数据导入任务分页
     */
    PageResult<ImportTaskDO> getImportTaskPage(ImportTaskPageReqVO pageReqVO);

}