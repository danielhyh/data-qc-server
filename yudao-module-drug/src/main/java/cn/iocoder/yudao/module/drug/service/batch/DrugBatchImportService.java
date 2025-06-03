package cn.iocoder.yudao.module.drug.service.batch;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.drug.controller.admin.batch.vo.*;
import cn.iocoder.yudao.module.drug.dal.dataobject.batch.ImportTaskDO;
import cn.iocoder.yudao.module.drug.dal.dataobject.batch.ImportTaskDetailDO;
import cn.iocoder.yudao.module.drug.enums.RetryTypeEnum;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 药品数据批量导入服务
 * 
 * 设计理念：
 * 1. 统一的任务管理：所有导入操作都通过任务来管理，便于跟踪和控制
 * 2. 分阶段处理：解压 -> 解析 -> 导入 -> 质控，每个阶段独立监控
 * 3. 智能重试：支持按阶段、按文件类型的精确重试
 * 4. 实时进度：提供多层次的进度反馈
 */
public interface DrugBatchImportService {
    
    /**
     * 创建批量导入任务
     *
     * @param file        压缩包文件
     * @param taskName    任务名称
     * @param description 任务描述
     * @return 任务信息
     */
    ImportTaskCreateResult createImportTask(MultipartFile file, String taskName, String description);
    
    /**
     * 获取任务详细信息
     * @param taskId 任务ID
     * @return 任务详情，包含所有明细
     */
    ImportTaskDetailDO getTaskDetail(Long taskId);
    
    /**
     * 获取任务实时进度
     * @param taskId 任务ID
     * @return 实时进度信息
     */
    ImportProgressVO getTaskProgress(Long taskId);
    
    /**
     * 重试失败的导入任务
     * @param taskId 任务ID
     * @param retryType 重试类型：ALL-全部重试，FAILED-仅失败部分，FILE_TYPE-指定文件类型
     * @param fileType 文件类型（当retryType为FILE_TYPE时必填）
     */
    ImportRetryResult retryImport(Long taskId, RetryTypeEnum retryType, String fileType);
    
    /**
     * 取消正在进行的任务
     * @param taskId 任务ID
     */
    void cancelTask(Long taskId);
    
    /**
     * 分页查询导入任务列表
     * @param pageReqVO 查询参数
     * @return 分页结果
     */
    PageResult<ImportTaskDO> getTaskPage(ImportTaskPageReqVO pageReqVO);

    /**
     * 验证导入文件
     * @param file 待验证文件
     * @return 验证结果
     */
    FileValidationResult validateImportFile(MultipartFile file);

    /**
     * 导出任务列表
     * @param pageReqVO 查询参数
     * @param response HTTP响应对象
     * @throws IOException IO异常
     */
    void exportTaskList(ImportTaskPageReqVO pageReqVO, HttpServletResponse response) throws IOException;

    /**
     * 获取任务执行日志
     * @param taskId 任务ID
     * @param logLevel 日志级别
     * @return 任务日志信息
     */
    TaskLogVO getTaskLogs(Long taskId, String logLevel);
}