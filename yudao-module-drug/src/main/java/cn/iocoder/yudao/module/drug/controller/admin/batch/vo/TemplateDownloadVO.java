package cn.iocoder.yudao.module.drug.controller.admin.batch.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 模板下载结果VO
 * 
 * 模板下载相关信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class TemplateDownloadVO {
    
    /**
     * 模板名称
     */
    private String templateName;
    
    /**
     * 下载链接
     */
    private String downloadUrl;
    
    /**
     * 模板版本
     */
    private String templateVersion;
    
    /**
     * 模板描述
     */
    private String description;
    
    /**
     * 文件数量
     */
    private Integer fileCount;
    
    /**
     * 模板大小
     */
    private String templateSize;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
