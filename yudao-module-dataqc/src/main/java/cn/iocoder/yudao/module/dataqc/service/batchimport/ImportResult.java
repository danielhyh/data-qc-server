/**
 * 导入结果封装类
 * 
 * 这个类用于封装每个文件的导入结果
 * 包含了详细的统计信息和错误详情
 */

package cn.iocoder.yudao.module.dataqc.service.batchimport;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ImportResult {
    
    // 基础信息
    private boolean success = true;
    private String message;
    private String fileType;
    private String fileName;
    
    // 统计信息
    private int totalCount = 0;      // 总记录数
    private int successCount = 0;    // 成功记录数
    private int insertCount = 0;     // 新增记录数
    private int updateCount = 0;     // 更新记录数
    private int failCount = 0;       // 失败记录数
    private int skipCount = 0;       // 跳过记录数
    
    // 时间信息
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long durationMs;         // 处理耗时（毫秒）
    
    // 错误详情
    private List<ImportError> errors = new ArrayList<>();
    
    // 警告信息
    private List<String> warnings = new ArrayList<>();
    
    /**
     * 创建成功结果
     */
    public static ImportResult success(String message) {
        ImportResult result = new ImportResult();
        result.setSuccess(true);
        result.setMessage(message);
        return result;
    }
    
    /**
     * 创建失败结果
     */
    public static ImportResult error(String message) {
        ImportResult result = new ImportResult();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }
    
    /**
     * 添加错误信息
     */
    public void addError(int rowNum, String field, String message) {
        this.errors.add(new ImportError(rowNum, field, message));
        this.failCount++;
    }
    
    /**
     * 添加警告信息
     */
    public void addWarning(String warning) {
        this.warnings.add(warning);
    }
    
    /**
     * 计算成功率
     */
    public double getSuccessRate() {
        if (totalCount == 0) return 0.0;
        return (double) successCount / totalCount * 100;
    }
    
    /**
     * 获取处理速度（记录/秒）
     */
    public double getProcessingSpeed() {
        if (durationMs == 0) return 0.0;
        return (double) totalCount / (durationMs / 1000.0);
    }
    
    /**
     * 导入错误详情
     */
    @Data
    public static class ImportError {
        private int rowNum;      // 行号
        private String field;    // 字段名
        private String message;  // 错误信息
        private String value;    // 错误的值
        
        public ImportError(int rowNum, String field, String message) {
            this.rowNum = rowNum;
            this.field = field;
            this.message = message;
        }
        
        public ImportError(int rowNum, String field, String message, String value) {
            this.rowNum = rowNum;
            this.field = field;
            this.message = message;
            this.value = value;
        }
    }
}