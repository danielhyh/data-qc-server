package cn.iocoder.yudao.module.dataqc.service.batchimport;

import cn.iocoder.yudao.module.dataqc.controller.admin.batchimport.vo.BatchImportTaskDetailRespVO;
import cn.iocoder.yudao.module.dataqc.controller.admin.batchimport.vo.BatchImportTaskRespVO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.batchimport.BatchImportTaskDO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 批量导入服务接口
 */
public interface IBatchImportService {
    
    /**
     * 批量导入压缩包
     * @param file 压缩包文件
     * @return 导入结果
     */
    BatchImportTaskRespVO batchImport(MultipartFile file) throws Exception;
    
    /**
     * 查询导入任务列表
     */
    List<BatchImportTaskDO> selectTaskList(BatchImportTaskDO task);
    
    /**
     * 查询导入任务详情
     */
    BatchImportTaskDO selectTaskById(Long taskId);
    
    /**
     * 查询导入任务明细
     */
    List<BatchImportTaskDetailRespVO> selectTaskDetailList(Long taskId);
    
    /**
     * 重新导入失败的文件
     */
    void retryImport(Long taskId, String fileType) throws Exception;
}