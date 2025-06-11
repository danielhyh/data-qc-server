package cn.iocoder.yudao.module.drug.controller.admin.batch.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 药品数据导入任务分页 Request VO")
@Data
public class ImportTaskPageReqVO extends PageParam {

    @Schema(description = "任务编号（格式：DRUG_YYYYMMDD_XXXXXX）")
    private String taskNo;

    @Schema(description = "任务名称", example = "王五")
    private String taskName;

    @Schema(description = "原始文件名称", example = "李四")
    private String fileName;

    @Schema(description = "文件存储路径")
    private String filePath;

    @Schema(description = "文件大小(字节)")
    private Long fileSize;

    @Schema(description = "解压后的文件列表(JSON格式)")
    private String extractedFiles;

    @Schema(description = "任务状态:0-待处理,1-解压中,2-数据导入中,3-质控中,4-完成,5-失败,6-部分成功", example = "2")
    private Integer status;

    @Schema(description = "解压状态:0-未开始,1-进行中,2-成功,3-失败", example = "2")
    private Integer extractStatus;

    @Schema(description = "导入状态:0-未开始,1-进行中,2-成功,3-失败", example = "1")
    private Integer importStatus;

    @Schema(description = "质控状态:0-未开始,1-进行中,2-成功,3-失败", example = "2")
    private Integer qcStatus;

    @Schema(description = "预期文件数量")
    private Integer totalFiles;

    @Schema(description = "成功文件数")
    private Integer successFiles;

    @Schema(description = "失败文件数")
    private Integer failedFiles;

    @Schema(description = "总记录数")
    private Long totalRecords;

    @Schema(description = "成功记录数")
    private Long successRecords;

    @Schema(description = "失败记录数")
    private Long failedRecords;

    @Schema(description = "整体进度百分比")
    private BigDecimal progressPercent;

    @Schema(description = "各表处理进度(JSON格式)")
    private String tableProgress;

    @Schema(description = "开始处理时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] startTime;

    @Schema(description = "解压完成时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] extractEndTime;

    @Schema(description = "导入完成时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] importEndTime;

    @Schema(description = "质控完成时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] qcEndTime;

    @Schema(description = "任务结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] endTime;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "详细错误信息(JSON格式)")
    private String errorDetail;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}