package cn.iocoder.yudao.module.dataqc.controller.admin.importlog.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 数据导入日志 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ImportLogRespVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10086")
    @ExcelProperty("主键ID")
    private Long id;

    @Schema(description = "批次号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("批次号")
    private String batchNo;

    @Schema(description = "文件名", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @ExcelProperty("文件名")
    private String fileName;

    @Schema(description = "文件类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("文件类型")
    private String fileType;

    @Schema(description = "目标表名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @ExcelProperty("目标表名")
    private String tableName;

    @Schema(description = "总行数")
    @ExcelProperty("总行数")
    private Integer totalRows;

    @Schema(description = "成功行数")
    @ExcelProperty("成功行数")
    private Integer successRows;

    @Schema(description = "失败行数")
    @ExcelProperty("失败行数")
    private Integer failRows;

    @Schema(description = "状态(PROCESSING-处理中,SUCCESS-成功,FAIL-失败)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("状态(PROCESSING-处理中,SUCCESS-成功,FAIL-失败)")
    private String status;

    @Schema(description = "错误信息")
    @ExcelProperty("错误信息")
    private String errorMsg;

    @Schema(description = "开始时间")
    @ExcelProperty("开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @ExcelProperty("结束时间")
    private LocalDateTime endTime;

    @Schema(description = "操作人ID", example = "3771")
    @ExcelProperty("操作人ID")
    private Long operatorId;

    @Schema(description = "操作人姓名", example = "赵六")
    @ExcelProperty("操作人姓名")
    private String operatorName;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}