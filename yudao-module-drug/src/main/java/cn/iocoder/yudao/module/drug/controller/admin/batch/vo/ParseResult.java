// ==================== 解析结果 ====================
package cn.iocoder.yudao.module.drug.controller.admin.batch.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Excel解析结果
 * 设计理念：封装文件解析的完整结果，支持数据验证和错误追踪
 */
@Data
@Builder
public class ParseResult {
    
    private Boolean success;              // 解析是否成功
    private String errorMessage;          // 错误消息
    
    private String fileName;              // 文件名
    private String tableType;             // 表类型
    
    private Integer totalRows;            // 总行数（包括标题行）
    private Integer dataRows;             // 数据行数
    private Integer validRows;            // 有效行数
    private Integer invalidRows;          // 无效行数
    
    private List<Object> dataList;        // 解析后的数据列表
    private List<ParseError> parseErrors; // 解析错误列表
    
    private Map<String, Object> metadata; // 元数据信息
    
    @Data
    @Builder
    public static class ParseError {
        private Integer rowIndex;         // 错误行号
        private String columnName;        // 错误列名
        private String errorType;         // 错误类型
        private String errorMessage;      // 错误描述
        private Object originalValue;     // 原始值
    }
}
