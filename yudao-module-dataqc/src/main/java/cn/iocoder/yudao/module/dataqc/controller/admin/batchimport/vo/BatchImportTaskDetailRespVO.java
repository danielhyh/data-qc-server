package cn.iocoder.yudao.module.dataqc.controller.admin.batchimport.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 批量导入任务明细 Response VO")
@Data
@ExcelIgnoreUnannotated
public class BatchImportTaskDetailRespVO {

    @Schema(description = "明细ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "26486")
    @ExcelProperty("明细ID")
    private Long id;

    @Schema(description = "任务ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "5976")
    @ExcelProperty("任务ID")
    private Long taskId;

    @Schema(description = "任务编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("任务编号")
    private String taskNo;

    @Schema(description = "文件类型(HOSPITAL_INFO,DRUG_LIST,DRUG_IN,DRUG_OUT,DRUG_USE)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("文件类型(HOSPITAL_INFO,DRUG_LIST,DRUG_IN,DRUG_OUT,DRUG_USE)")
    private String fileType;

    @Schema(description = "文件名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @ExcelProperty("文件名")
    private String fileName;

    @Schema(description = "目标表名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @ExcelProperty("目标表名")
    private String tableName;

    @Schema(description = "状态(0-待处理,1-处理中,2-成功,3-失败)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("状态(0-待处理,1-处理中,2-成功,3-失败)")
    private Integer status;

    @Schema(description = "总行数")
    @ExcelProperty("总行数")
    private Integer totalRows;

    @Schema(description = "成功行数")
    @ExcelProperty("成功行数")
    private Integer successRows;

    @Schema(description = "失败行数")
    @ExcelProperty("失败行数")
    private Integer failRows;

    @Schema(description = "开始时间")
    @ExcelProperty("开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @ExcelProperty("结束时间")
    private LocalDateTime endTime;

    @Schema(description = "错误信息")
    @ExcelProperty("错误信息")
    private String errorMsg;

    @Schema(description = "错误详情(JSON格式)")
    @ExcelProperty("错误详情(JSON格式)")
    private String errorDetail;

    @Schema(description = "导入批次号")
    @ExcelProperty("导入批次号")
    private String importBatchNo;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}