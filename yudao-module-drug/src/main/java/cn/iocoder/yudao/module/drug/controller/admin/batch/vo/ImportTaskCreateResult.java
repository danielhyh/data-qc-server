// ==================== 任务创建结果 ====================
package cn.iocoder.yudao.module.drug.controller.admin.batch.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 药品导入任务创建结果
 * 设计理念：提供任务创建后的关键信息，支持前端立即展示和跟踪
 */
@Data
@Builder
public class ImportTaskCreateResult {
    
    /**
     * 任务ID - 用于后续所有操作的标识
     */
    private Long taskId;
    
    /**
     * 任务编号 - 用户友好的标识符
     */
    private String taskNo;
    
    /**
     * 创建成功消息
     */
    private String message;
    
    /**
     * 任务创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 预计完成时间
     */
    private LocalDateTime estimatedCompletionTime;
    
    /**
     * 文件基本信息
     */
    private FileBasicInfo fileInfo;
    
    @Data
    @Builder
    public static class FileBasicInfo {
        private String originalFileName;
        private Long fileSize;
        private Integer expectedFileCount;
    }
}