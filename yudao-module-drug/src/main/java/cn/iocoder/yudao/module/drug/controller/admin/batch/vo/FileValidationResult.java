package cn.iocoder.yudao.module.drug.controller.admin.batch.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件验证结果VO
 * 
 * 用于前端展示文件验证的详细结果信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileValidationResult {
    
    /**
     * 验证是否通过
     */
    private Boolean valid;
    
    /**
     * 文件名称
     */
    private String fileName;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 预期文件数量
     */
    private Integer expectedFileCount;
    
    /**
     * 实际文件数量
     */
    private Integer actualFileCount;
    
    /**
     * 验证结果消息
     */
    private String validationMessage;
    
    /**
     * 缺失的文件列表
     */
    private List<String> missingFiles;
    
    /**
     * 多余的文件列表
     */
    private List<String> extraFiles;
    
    /**
     * 无效的文件列表
     */
    private List<String> invalidFiles;
    
    /**
     * 验证时间
     */
    private LocalDateTime validationTime;
}
