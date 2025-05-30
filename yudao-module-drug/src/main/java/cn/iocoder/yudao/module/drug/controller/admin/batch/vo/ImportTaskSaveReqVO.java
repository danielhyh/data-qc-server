package cn.iocoder.yudao.module.drug.controller.admin.batch.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 药品数据导入任务新增/修改 Request VO")
@Data
public class ImportTaskSaveReqVO {

    @Schema(description = "任务ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "20466")
    private Long id;

    @Schema(description = "任务编号（格式：DRUG_YYYYMMDD_XXXXXX）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "任务编号（格式：DRUG_YYYYMMDD_XXXXXX）不能为空")
    private String taskNo;

    @Schema(description = "任务名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @NotEmpty(message = "任务名称不能为空")
    private String taskName;

    @Schema(description = "导入类型:1-单文件,2-压缩包", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "导入类型:1-单文件,2-压缩包不能为空")
    private Integer importType;

    @Schema(description = "原始文件名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotEmpty(message = "原始文件名称不能为空")
    private String fileName;

    @Schema(description = "文件存储路径", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "文件存储路径不能为空")
    private String filePath;

    @Schema(description = "文件大小(字节)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "文件大小(字节)不能为空")
    private Long fileSize;

    @Schema(description = "解压后的文件列表(JSON格式)")
    private String extractedFiles;

    @Schema(description = "任务状态:0-待处理,1-解压中,2-数据导入中,3-质控中,4-完成,5-失败,6-部分成功", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "任务状态:0-待处理,1-解压中,2-数据导入中,3-质控中,4-完成,5-失败,6-部分成功不能为空")
    private Integer status;

    @Schema(description = "解压状态:0-未开始,1-进行中,2-成功,3-失败", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "解压状态:0-未开始,1-进行中,2-成功,3-失败不能为空")
    private Integer extractStatus;

    @Schema(description = "导入状态:0-未开始,1-进行中,2-成功,3-失败", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "导入状态:0-未开始,1-进行中,2-成功,3-失败不能为空")
    private Integer importStatus;

    @Schema(description = "质控状态:0-未开始,1-进行中,2-成功,3-失败", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "质控状态:0-未开始,1-进行中,2-成功,3-失败不能为空")
    private Integer qcStatus;

    @Schema(description = "预期文件数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "预期文件数量不能为空")
    private Integer totalFiles;

    @Schema(description = "成功文件数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "成功文件数不能为空")
    private Integer successFiles;

    @Schema(description = "失败文件数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "失败文件数不能为空")
    private Integer failedFiles;

    @Schema(description = "总记录数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "总记录数不能为空")
    private Long totalRecords;

    @Schema(description = "成功记录数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "成功记录数不能为空")
    private Long successRecords;

    @Schema(description = "失败记录数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "失败记录数不能为空")
    private Long failedRecords;

    @Schema(description = "整体进度百分比", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "整体进度百分比不能为空")
    private BigDecimal progressPercent;

    @Schema(description = "各表处理进度(JSON格式)")
    private String tableProgress;

    @Schema(description = "开始处理时间")
    private LocalDateTime startTime;

    @Schema(description = "解压完成时间")
    private LocalDateTime extractEndTime;

    @Schema(description = "导入完成时间")
    private LocalDateTime importEndTime;

    @Schema(description = "质控完成时间")
    private LocalDateTime qcEndTime;

    @Schema(description = "任务结束时间")
    private LocalDateTime endTime;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "详细错误信息(JSON格式)")
    private String errorDetail;

}