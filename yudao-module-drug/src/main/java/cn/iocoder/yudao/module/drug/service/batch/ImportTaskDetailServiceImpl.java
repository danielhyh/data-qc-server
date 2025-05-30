package cn.iocoder.yudao.module.drug.service.batch;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.drug.controller.admin.batch.vo.ImportTaskDetailPageReqVO;
import cn.iocoder.yudao.module.drug.controller.admin.batch.vo.ImportTaskDetailSaveReqVO;
import cn.iocoder.yudao.module.drug.dal.dataobject.batch.ImportTaskDetailDO;
import cn.iocoder.yudao.module.drug.dal.mysql.batch.ImportTaskDetailMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.drug.enums.DrugErrorCodeConstants.TASK_NOT_FOUND;

/**
 * 药品数据导入任务明细 Service 实现类
 *
 * @author hyh
 */
@Service
@Validated
public class ImportTaskDetailServiceImpl implements ImportTaskDetailService {

    @Resource
    private ImportTaskDetailMapper importTaskDetailMapper;

    @Override
    public Long createImportTaskDetail(ImportTaskDetailSaveReqVO createReqVO) {
        // 插入
        ImportTaskDetailDO importTaskDetail = BeanUtils.toBean(createReqVO, ImportTaskDetailDO.class);
        importTaskDetailMapper.insert(importTaskDetail);
        // 返回
        return importTaskDetail.getId();
    }

    @Override
    public void updateImportTaskDetail(ImportTaskDetailSaveReqVO updateReqVO) {
        // 校验存在
        validateImportTaskDetailExists(updateReqVO.getId());
        // 更新
        ImportTaskDetailDO updateObj = BeanUtils.toBean(updateReqVO, ImportTaskDetailDO.class);
        importTaskDetailMapper.updateById(updateObj);
    }

    @Override
    public void deleteImportTaskDetail(Long id) {
        // 校验存在
        validateImportTaskDetailExists(id);
        // 删除
        importTaskDetailMapper.deleteById(id);
    }

    @Override
        public void deleteImportTaskDetailListByIds(List<Long> ids) {
        // 校验存在
        validateImportTaskDetailExists(ids);
        // 删除
        importTaskDetailMapper.deleteByIds(ids);
        }

    private void validateImportTaskDetailExists(List<Long> ids) {
        List<ImportTaskDetailDO> list = importTaskDetailMapper.selectByIds(ids);
        if (CollUtil.isEmpty(list) || list.size() != ids.size()) {
            throw exception(TASK_NOT_FOUND);
        }
    }

    private void validateImportTaskDetailExists(Long id) {
        if (importTaskDetailMapper.selectById(id) == null) {
            throw exception(TASK_NOT_FOUND);
        }
    }

    @Override
    public ImportTaskDetailDO getImportTaskDetail(Long id) {
        return importTaskDetailMapper.selectById(id);
    }

    @Override
    public PageResult<ImportTaskDetailDO> getImportTaskDetailPage(ImportTaskDetailPageReqVO pageReqVO) {
        return importTaskDetailMapper.selectPage(pageReqVO);
    }

}