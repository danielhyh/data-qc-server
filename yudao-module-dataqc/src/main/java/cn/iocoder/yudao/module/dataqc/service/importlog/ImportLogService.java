package cn.iocoder.yudao.module.dataqc.service.importlog;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.dataqc.controller.admin.importlog.vo.ImportLogPageReqVO;
import cn.iocoder.yudao.module.dataqc.controller.admin.importlog.vo.ImportLogSaveReqVO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.importlog.ImportLogDO;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 数据导入日志 Service 接口
 *
 * @author 管理员
 */
public interface ImportLogService {

    /**
     * 创建数据导入日志
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createImportLog(@Valid ImportLogSaveReqVO createReqVO);

    /**
     * 更新数据导入日志
     *
     * @param updateReqVO 更新信息
     */
    void updateImportLog(@Valid ImportLogSaveReqVO updateReqVO);

    @Transactional(rollbackFor = Exception.class)
    void updateImportLogFail(Long logId, String errorMessage);

    List<ImportLogDO> getImportLogList(String batchNo, String status);

    /**
     * 删除数据导入日志
     *
     * @param id 编号
     */
    void deleteImportLog(Long id);

    /**
    * 批量删除数据导入日志
    *
    * @param ids 编号
    */
    void deleteImportLogListByIds(List<Long> ids);

    /**
     * 获得数据导入日志
     *
     * @param id 编号
     * @return 数据导入日志
     */
    ImportLogDO getImportLog(Long id);

    /**
     * 获得数据导入日志分页
     *
     * @param pageReqVO 分页查询
     * @return 数据导入日志分页
     */
    PageResult<ImportLogDO> getImportLogPage(ImportLogPageReqVO pageReqVO);

}