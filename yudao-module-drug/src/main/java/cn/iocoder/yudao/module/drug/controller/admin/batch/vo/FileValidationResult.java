package cn.iocoder.yudao.module.drug.controller.admin.batch.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 文件验证结果VO - 增强版
 *
 * 用于前端展示文件验证的详细结果信息，包含解压文件清单和数据预览
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

    // ========== 新增字段：详细的文件信息 ==========

    /**
     * 解压文件清单
     * 这是前端步骤2需要展示的核心数据
     */
    private List<ExtractedFileInfo> extractedFiles;

    /**
     * 解压耗时（毫秒）
     */
    private Long extractDurationMs;

    /**
     * 解压开始时间
     */
    private LocalDateTime extractStartTime;

    /**
     * 解压结束时间
     */
    private LocalDateTime extractEndTime;

    /**
     * 解压后的文件信息内部类
     * 对应前端需要的文件详情数据结构
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtractedFileInfo {
        /**
         * 文件名
         */
        private String fileName;

        /**
         * 数据表类型（对应TableTypeEnum的name）
         */
        private String tableType;

        /**
         * 数据行数
         */
        private Integer rowCount;

        /**
         * 文件大小（字节）
         */
        private Long fileSize;

        /**
         * 验证状态
         */
        private Boolean isValid;

        /**
         * 实际解析到的字段列表
         */
        private List<String> actualFields;

        /**
         * 数据预览（前5行）
         */
        private List<Map<String, Object>> previewData;

        /**
         * 有效数据行数
         */
        private Integer validRowCount;

        /**
         * 数据质量等级（HIGH/MEDIUM/LOW）
         */
        private String dataQuality;

        /**
         * 文件编码格式
         */
        private String encoding;

        /**
         * 数据质量详情
         */
        private FileInfo.DataQualityInfo qualityInfo;
    }
}