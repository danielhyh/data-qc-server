package cn.iocoder.yudao.module.dataqc.controller.admin.batchimport.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 批量导入任务 Response VO")
@Data
@ExcelIgnoreUnannotated
public class BatchImportTaskRespVO {

    @Schema(description = "任务ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10680")
    @ExcelProperty("任务ID")
    private Long id;

    @Schema(description = "任务编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("任务编号")
    private String taskNo;

    @Schema(description = "任务名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @ExcelProperty("任务名称")
    private String taskName;

    @Schema(description = "上传文件名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @ExcelProperty("上传文件名")
    private String fileName;

    @Schema(description = "文件存储路径")
    @ExcelProperty("文件存储路径")
    private String filePath;

    @Schema(description = "状态(0-待处理,1-处理中,2-成功,3-失败,4-部分成功)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("状态(0-待处理,1-处理中,2-成功,3-失败,4-部分成功)")
    private Integer status;

    @Schema(description = "文件总数")
    @ExcelProperty("文件总数")
    private Integer totalFiles;

    @Schema(description = "成功文件数")
    @ExcelProperty("成功文件数")
    private Integer successFiles;

    @Schema(description = "失败文件数")
    @ExcelProperty("失败文件数")
    private Integer failFiles;

    @Schema(description = "开始时间")
    @ExcelProperty("开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @ExcelProperty("结束时间")
    private LocalDateTime endTime;

    @Schema(description = "错误信息")
    @ExcelProperty("错误信息")
    private String errorMsg;

    @Schema(description = "结果详情(JSON格式)")
    @ExcelProperty("结果详情(JSON格式)")
    private String resultDetail;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "消息", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("消息")
    private String message;

}