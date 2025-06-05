package cn.iocoder.yudao.module.drug.controller.admin.batch.vo;

import cn.iocoder.yudao.module.drug.enums.TableTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 单个文件信息
 * <p>
 * 这个类封装了单个Excel文件的所有相关信息
 * 设计时考虑了文件处理的完整生命周期需求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileInfo {

    /**
     * 文件名（不含路径）
     * 例如："机构基本情况.xlsx"
     */
    private String fileName;

    /**
     * 完整文件路径
     * 例如："/tmp/drug-import/task_123/机构基本情况.xlsx"
     */
    private String filePath;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件最后修改时间
     * 有助于判断文件的新旧程度
     */
    private LocalDateTime lastModified;

    /**
     * 对应的表类型
     * 建立文件与业务表的明确映射关系
     */
    private TableTypeEnum tableType;

    /**
     * 文件验证状态
     * true: 文件格式正确，可以进行后续处理
     * false: 文件存在问题，需要人工干预
     */
    private Boolean isValid;

    /**
     * 验证失败原因
     * 当isValid为false时记录具体原因
     */
    private String validationError;

    /**
     * 预估的数据行数
     * 通过快速扫描Excel文件获得，用于进度计算
     * -1表示未计算或计算失败
     */
    private Integer estimatedRowCount;

    /**
     * Excel工作表数量
     * 用于验证文件结构是否符合预期
     */
    private Integer sheetCount;

    /**
     * 主要工作表名称
     * 大多数情况下数据都在第一个工作表中
     */
    private String primarySheetName;

    /**
     * 文件处理优先级
     * 数值越小优先级越高，用于控制导入顺序
     * 例如：机构信息(1) > 药品目录(2) > 其他业务数据(3)
     */
    private Integer processingPriority;

    /**
     * 实际解析到的字段列表
     * 这是Excel文件第一行（标题行）的实际内容
     */
    private List<String> actualFields;
    /**
     * 数据预览 - 前5行的实际数据
     * 使用Map<String, Object>来保持数据的灵活性
     */
    private List<Map<String, Object>> previewData;
    /**
     * 有效数据行数（排除空行）
     */
    private Integer validRowCount;
    /**
     * 数据质量评估
     */
    private String dataQuality; // HIGH/MEDIUM/LOW
    /**
     * 文件编码格式
     */
    private String encoding;
    /**
     * 数据质量详情
     */
    private DataQualityInfo qualityInfo;

    /**
     * 便捷方法：获取文件大小的易读格式
     *
     * @return 格式化的文件大小，如 "1.5 MB"
     */
    public String getFormattedFileSize() {
        if (fileSize == null) return "未知";

        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        }
    }

    /**
     * 便捷方法：检查文件是否可以处理
     *
     * @return 文件是否准备就绪可以处理
     */
    public boolean isReadyForProcessing() {
        return isValid != null && isValid && filePath != null && !filePath.isEmpty();
    }

    /**
     * 数据质量信息内部类
     * 提供细粒度的质量评估信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataQualityInfo {
        /**
         * 必填字段缺失数量
         */
        private Integer missingRequiredFields;

        /**
         * 空值数量
         */
        private Integer nullValueCount;

        /**
         * 重复行数量
         */
        private Integer duplicateRowCount;

        /**
         * 数据完整性评分（0-100）
         */
        private Integer completenessScore;

        /**
         * 质量问题列表
         */
        private List<String> qualityIssues;
    }
}