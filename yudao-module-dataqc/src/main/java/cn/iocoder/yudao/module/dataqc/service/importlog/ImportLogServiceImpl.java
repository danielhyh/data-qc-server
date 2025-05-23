package cn.iocoder.yudao.module.dataqc.service.importlog;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.dataqc.controller.admin.importlog.vo.ImportLogPageReqVO;
import cn.iocoder.yudao.module.dataqc.controller.admin.importlog.vo.ImportLogSaveReqVO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.importlog.ImportLogDO;
import cn.iocoder.yudao.module.dataqc.dal.mysql.importlog.ImportLogMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.dataqc.enums.ErrorCodeConstants.IMPORT_LOG_NOT_EXISTS;

/**
 * 数据导入日志 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class ImportLogServiceImpl implements ImportLogService {

    @Resource
    private ImportLogMapper importLogMapper;

    @Override
    public Long createImportLog(ImportLogSaveReqVO createReqVO) {
        // 插入
        ImportLogDO importLog = BeanUtils.toBean(createReqVO, ImportLogDO.class);
        importLogMapper.insert(importLog);
        // 返回
        return importLog.getId();
    }

    @Override
    public void updateImportLog(ImportLogSaveReqVO updateReqVO) {
        // 校验存在
        validateImportLogExists(updateReqVO.getId());
        // 更新
        ImportLogDO updateObj = BeanUtils.toBean(updateReqVO, ImportLogDO.class);
        importLogMapper.updateById(updateObj);
    }

    @Override
    public void deleteImportLog(Long id) {
        // 校验存在
        validateImportLogExists(id);
        // 删除
        importLogMapper.deleteById(id);
    }

    @Override
        public void deleteImportLogListByIds(List<Long> ids) {
        // 校验存在
        validateImportLogExists(ids);
        // 删除
        importLogMapper.deleteByIds(ids);
        }

    private void validateImportLogExists(List<Long> ids) {
        List<ImportLogDO> list = importLogMapper.selectByIds(ids);
        if (CollUtil.isEmpty(list) || list.size() != ids.size()) {
            throw exception(IMPORT_LOG_NOT_EXISTS);
        }
    }

    private void validateImportLogExists(Long id) {
        if (importLogMapper.selectById(id) == null) {
            throw exception(IMPORT_LOG_NOT_EXISTS);
        }
    }

    @Override
    public ImportLogDO getImportLog(Long id) {
        return importLogMapper.selectById(id);
    }

    @Override
    public PageResult<ImportLogDO> getImportLogPage(ImportLogPageReqVO pageReqVO) {
        return importLogMapper.selectPage(pageReqVO);
    }

}