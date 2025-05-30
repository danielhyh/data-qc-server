package cn.iocoder.yudao.module.drug.service.batch;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.drug.controller.admin.batch.vo.ImportTaskPageReqVO;
import cn.iocoder.yudao.module.drug.controller.admin.batch.vo.ImportTaskSaveReqVO;
import cn.iocoder.yudao.module.drug.dal.dataobject.batch.ImportTaskDO;
import cn.iocoder.yudao.module.drug.dal.mysql.batch.ImportTaskMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.drug.enums.DrugErrorCodeConstants.TASK_NOT_FOUND;

/**
 * 药品数据导入任务 Service 实现类
 *
 * @author hyh
 */
@Service
@Validated
public class ImportTaskServiceImpl implements ImportTaskService {

    @Resource
    private ImportTaskMapper importTaskMapper;

    @Override
    public Long createImportTask(ImportTaskSaveReqVO createReqVO) {
        // 插入
        ImportTaskDO importTask = BeanUtils.toBean(createReqVO, ImportTaskDO.class);
        importTaskMapper.insert(importTask);
        // 返回
        return importTask.getId();
    }

    @Override
    public void updateImportTask(ImportTaskSaveReqVO updateReqVO) {
        // 校验存在
        validateImportTaskExists(updateReqVO.getId());
        // 更新
        ImportTaskDO updateObj = BeanUtils.toBean(updateReqVO, ImportTaskDO.class);
        importTaskMapper.updateById(updateObj);
    }

    @Override
    public void deleteImportTask(Long id) {
        // 校验存在
        validateImportTaskExists(id);
        // 删除
        importTaskMapper.deleteById(id);
    }

    @Override
    public void deleteImportTaskListByIds(List<Long> ids) {
        // 校验存在
        validateImportTaskExists(ids);
        // 删除
        importTaskMapper.deleteByIds(ids);
    }

    private void validateImportTaskExists(List<Long> ids) {
        List<ImportTaskDO> list = importTaskMapper.selectByIds(ids);
        if (CollUtil.isEmpty(list) || list.size() != ids.size()) {
            throw exception(TASK_NOT_FOUND);
        }
    }

    private void validateImportTaskExists(Long id) {
        if (importTaskMapper.selectById(id) == null) {
            throw exception(TASK_NOT_FOUND);
        }
    }

    @Override
    public ImportTaskDO getImportTask(Long id) {
        return importTaskMapper.selectById(id);
    }

    @Override
    public PageResult<ImportTaskDO> getImportTaskPage(ImportTaskPageReqVO pageReqVO) {
        return importTaskMapper.selectPage(pageReqVO);
    }

}