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
 * 文件提取结果
 * <p>
 * 这个类封装了文件解压和验证的完整结果，体现了"高内聚"的设计原则
 * 将相关数据和状态信息集中管理，便于后续流程的判断和处理
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileExtractResult {
    
    /**
     * 是否成功解压
     * 这是一个关键的状态标识，后续所有操作都依赖这个标志
     */
    private Boolean success;
    
    /**
     * 错误信息
     * 当success为false时，这里记录具体的错误原因
     * 遵循"快速失败"原则，让调用方能够及时了解问题所在
     */
    private String errorMessage;
    
    /**
     * 解压后的文件信息映射
     * Key: TableTypeEnum (表类型枚举)
     * Value: FileInfo (具体文件信息)
     * 
     * 这种设计的优势：
     * 1. 类型安全：使用枚举作为key避免字符串错误
     * 2. 快速查找：O(1)时间复杂度获取指定类型的文件信息
     * 3. 清晰映射：文件类型与业务表类型直接对应
     */
    private Map<TableTypeEnum, FileInfo> fileInfos;
    
    /**
     * 解压耗时（毫秒）
     * 用于性能监控和优化分析
     */
    private Long extractDurationMs;
    
    /**
     * 总文件数量
     * 便于统计和进度计算
     */
    private Integer totalFileCount;
    
    /**
     * 有效文件数量
     * 指符合业务规则的Excel文件数量
     */
    private Integer validFileCount;
    
    /**
     * 解压开始时间
     */
    private LocalDateTime extractStartTime;
    
    /**
     * 解压结束时间
     */
    private LocalDateTime extractEndTime;
    
    /**
     * 便捷方法：根据表类型获取文件信息
     * <p>
     * 这个方法体现了"易用性"设计原则，为调用方提供更简洁的API
     * 
     * @param tableType 表类型
     * @return 对应的文件信息，如果不存在则返回null
     */
    public FileInfo getFileInfo(TableTypeEnum tableType) {
        return fileInfos != null ? fileInfos.get(tableType) : null;
    }
    
    /**
     * 便捷方法：检查是否包含指定类型的文件
     * 
     * @param tableType 表类型
     * @return 是否包含该类型文件
     */
    public boolean hasFileType(TableTypeEnum tableType) {
        return fileInfos != null && fileInfos.containsKey(tableType);
    }
    
    /**
     * 便捷方法：获取所有有效的表类型
     * 
     * @return 表类型列表
     */
    public List<TableTypeEnum> getAvailableTableTypes() {
        return fileInfos != null ? List.copyOf(fileInfos.keySet()) : List.of();
    }
}
