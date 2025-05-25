package cn.iocoder.yudao.module.dataqc.service.importlog;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.dataqc.controller.admin.importlog.vo.ImportLogPageReqVO;
import cn.iocoder.yudao.module.dataqc.controller.admin.importlog.vo.ImportLogSaveReqVO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.importlog.ImportLogDO;
import cn.iocoder.yudao.module.dataqc.dal.mysql.importlog.ImportLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
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
@Slf4j
public class ImportLogServiceImpl implements ImportLogService {

    @Resource
    private ImportLogMapper importLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createImportLog(ImportLogSaveReqVO createReqVO) {
        // 创建导入日志记录
        ImportLogDO importLog = BeanUtils.toBean(createReqVO, ImportLogDO.class);
        importLog.setStatus("PROCESSING"); // 设置初始状态为处理中
        importLog.setStartTime(LocalDateTime.now());
        importLog.setCreateTime(LocalDateTime.now());

        importLogMapper.insert(importLog);

        log.info("创建导入日志记录，批次号：{}，文件名：{}",
                importLog.getBatchNo(), importLog.getFileName());

        return importLog.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateImportLog(ImportLogSaveReqVO updateReqVO) {
        if (updateReqVO.getId() == null) {
            log.warn("更新导入日志失败：ID不能为空");
            return;
        }

        ImportLogDO updateObj = BeanUtils.toBean(updateReqVO, ImportLogDO.class);
        updateObj.setEndTime(LocalDateTime.now());

        // 根据成功率判断最终状态
        if (updateReqVO.getFailRows() != null && updateReqVO.getSuccessRows() != null) {
            if (updateReqVO.getFailRows() == 0) {
                updateObj.setStatus("SUCCESS"); // 全部成功
            } else if (updateReqVO.getSuccessRows() > 0) {
                updateObj.setStatus("PARTIAL_SUCCESS"); // 部分成功
            } else {
                updateObj.setStatus("FAIL"); // 全部失败
            }
        }

        importLogMapper.updateById(updateObj);

        log.info("更新导入日志，ID：{}，状态：{}", updateReqVO.getId(), updateObj.getStatus());
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateImportLogFail(Long logId, String errorMessage) {
        if (logId == null) {
            log.warn("更新导入日志失败状态失败：ID不能为空");
            return;
        }

        ImportLogDO updateObj = new ImportLogDO();
        updateObj.setId(logId);
        updateObj.setStatus("FAIL");
        updateObj.setErrorMsg(errorMessage);
        updateObj.setEndTime(LocalDateTime.now());

        importLogMapper.updateById(updateObj);

        log.error("导入失败，日志ID：{}，错误信息：{}", logId, errorMessage);
    }
    @Override
    public List<ImportLogDO> getImportLogList(String batchNo, String status) {
        LambdaQueryWrapper<ImportLogDO> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(StrUtil.isNotEmpty(batchNo), ImportLogDO::getBatchNo, batchNo);
        wrapper.eq(StrUtil.isNotEmpty(status), ImportLogDO::getStatus, status);
        wrapper.orderByDesc(ImportLogDO::getCreateTime);

        return importLogMapper.selectList(wrapper);
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