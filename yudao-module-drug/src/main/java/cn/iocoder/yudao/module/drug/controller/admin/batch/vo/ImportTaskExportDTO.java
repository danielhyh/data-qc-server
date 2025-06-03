package cn.iocoder.yudao.module.drug.controller.admin.batch.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 任务导出DTO
 * 
 * 用于Excel导出的数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportTaskExportDTO {
    
    @ExcelProperty("任务编号")
    private String taskNo;
    
    @ExcelProperty("任务名称")
    private String taskName;
    
    @ExcelProperty("文件名称")
    private String fileName;
    
    @ExcelProperty("任务状态")
    private String statusDisplay;
    
    @ExcelProperty("总文件数")
    private Integer totalFiles;
    
    @ExcelProperty("成功文件数")
    private Integer successFiles;
    
    @ExcelProperty("失败文件数")
    private Integer failedFiles;
    
    @ExcelProperty("总记录数")
    private Long totalRecords;
    
    @ExcelProperty("成功记录数")
    private Long successRecords;
    
    @ExcelProperty("失败记录数")
    private Long failedRecords;
    
    @ExcelProperty("进度百分比")
    private Integer progressPercent;
    
    @ExcelProperty("开始时间")
    private LocalDateTime startTime;
    
    @ExcelProperty("结束时间")
    private LocalDateTime endTime;
    
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
    
    @ExcelProperty("创建人")
    private String creator;
}