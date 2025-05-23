package cn.iocoder.yudao.module.dataqc.controller.admin.batchimport.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 批量导入任务明细分页 Request VO")
@Data
public class BatchImportTaskDetailPageReqVO extends PageParam {

    @Schema(description = "任务ID", example = "5976")
    private Long taskId;

    @Schema(description = "任务编号")
    private String taskNo;

    @Schema(description = "文件类型(HOSPITAL_INFO,DRUG_LIST,DRUG_IN,DRUG_OUT,DRUG_USE)", example = "1")
    private String fileType;

    @Schema(description = "文件名", example = "张三")
    private String fileName;

    @Schema(description = "目标表名", example = "张三")
    private String tableName;

    @Schema(description = "状态(0-待处理,1-处理中,2-成功,3-失败)", example = "1")
    private Integer status;

    @Schema(description = "总行数")
    private Integer totalRows;

    @Schema(description = "成功行数")
    private Integer successRows;

    @Schema(description = "失败行数")
    private Integer failRows;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] startTime;

    @Schema(description = "结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] endTime;

    @Schema(description = "错误信息")
    private String errorMsg;

    @Schema(description = "错误详情(JSON格式)")
    private String errorDetail;

    @Schema(description = "导入批次号")
    private String importBatchNo;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}