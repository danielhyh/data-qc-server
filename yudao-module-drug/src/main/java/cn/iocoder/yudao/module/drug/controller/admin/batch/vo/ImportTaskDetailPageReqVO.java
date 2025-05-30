package cn.iocoder.yudao.module.drug.controller.admin.batch.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 药品数据导入任务明细分页 Request VO")
@Data
public class ImportTaskDetailPageReqVO extends PageParam {

    @Schema(description = "任务ID", example = "11474")
    private Long taskId;

    @Schema(description = "任务编号")
    private String taskNo;

    @Schema(description = "文件类型:HOSPITAL_INFO,DRUG_CATALOG,DRUG_INBOUND,DRUG_OUTBOUND,DRUG_USAGE", example = "2")
    private String fileType;

    @Schema(description = "文件名", example = "王五")
    private String fileName;

    @Schema(description = "目标表名")
    private String targetTable;

    @Schema(description = "表类型:1-机构信息,2-药品目录,3-入库情况,4-出库情况,5-使用情况", example = "1")
    private Integer tableType;

    @Schema(description = "状态:0-待处理,1-解析中,2-导入中,3-质控中,4-成功,5-失败", example = "1")
    private Integer status;

    @Schema(description = "解析状态:0-未开始,1-进行中,2-成功,3-失败", example = "2")
    private Integer parseStatus;

    @Schema(description = "导入状态:0-未开始,1-进行中,2-成功,3-失败", example = "1")
    private Integer importStatus;

    @Schema(description = "质控状态:0-未开始,1-进行中,2-成功,3-失败", example = "2")
    private Integer qcStatus;

    @Schema(description = "总行数")
    private Long totalRows;

    @Schema(description = "有效行数")
    private Long validRows;

    @Schema(description = "导入成功行数")
    private Long successRows;

    @Schema(description = "导入失败行数")
    private Long failedRows;

    @Schema(description = "质控通过行数")
    private Long qcPassedRows;

    @Schema(description = "质控失败行数")
    private Long qcFailedRows;

    @Schema(description = "处理进度百分比")
    private BigDecimal progressPercent;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] startTime;

    @Schema(description = "解析完成时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] parseEndTime;

    @Schema(description = "导入完成时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] importEndTime;

    @Schema(description = "质控完成时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] qcEndTime;

    @Schema(description = "结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] endTime;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "错误行详情(JSON格式)")
    private String errorRowsDetail;

    @Schema(description = "导入批次号")
    private String importBatchNo;

    @Schema(description = "重试次数", example = "14394")
    private Integer retryCount;

    @Schema(description = "最大重试次数", example = "14274")
    private Integer maxRetryCount;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}