package cn.iocoder.yudao.module.drug.service.batch;

import org.springframework.web.multipart.MultipartFile;

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
     * @param file 压缩包文件
     * @param taskName 任务名称
     * @return 任务信息
     */
    DrugImportTaskCreateResult createImportTask(MultipartFile file, String taskName);
    
    /**
     * 获取任务详细信息
     * @param taskId 任务ID
     * @return 任务详情，包含所有明细
     */
    DrugImportTaskDetailVO getTaskDetail(Long taskId);
    
    /**
     * 获取任务实时进度
     * @param taskId 任务ID
     * @return 实时进度信息
     */
    DrugImportProgressVO getTaskProgress(Long taskId);
    
    /**
     * 重试失败的导入任务
     * @param taskId 任务ID
     * @param retryType 重试类型：ALL-全部重试，FAILED-仅失败部分，FILE_TYPE-指定文件类型
     * @param fileType 文件类型（当retryType为FILE_TYPE时必填）
     */
    DrugImportRetryResult retryImport(Long taskId, RetryTypeEnum retryType, String fileType);
    
    /**
     * 取消正在进行的任务
     * @param taskId 任务ID
     */
    void cancelTask(Long taskId);
    
    /**
     * 分页查询导入任务列表
     * @param queryParam 查询参数
     * @return 分页结果
     */
    PageResult<DrugImportTaskVO> getTaskPage(DrugImportTaskPageReq queryParam);
}