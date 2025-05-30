package cn.iocoder.yudao.module.drug.controller.admin.batch.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 药品数据导入任务明细新增/修改 Request VO")
@Data
public class ImportTaskDetailSaveReqVO {

    @Schema(description = "明细ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1835")
    private Long id;

    @Schema(description = "任务ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "11474")
    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    @Schema(description = "任务编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "任务编号不能为空")
    private String taskNo;

    @Schema(description = "文件类型:HOSPITAL_INFO,DRUG_CATALOG,DRUG_INBOUND,DRUG_OUTBOUND,DRUG_USAGE", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "文件类型:HOSPITAL_INFO,DRUG_CATALOG,DRUG_INBOUND,DRUG_OUTBOUND,DRUG_USAGE不能为空")
    private String fileType;

    @Schema(description = "文件名", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @NotEmpty(message = "文件名不能为空")
    private String fileName;

    @Schema(description = "目标表名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "目标表名不能为空")
    private String targetTable;

    @Schema(description = "表类型:1-机构信息,2-药品目录,3-入库情况,4-出库情况,5-使用情况", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "表类型:1-机构信息,2-药品目录,3-入库情况,4-出库情况,5-使用情况不能为空")
    private Integer tableType;

    @Schema(description = "状态:0-待处理,1-解析中,2-导入中,3-质控中,4-成功,5-失败", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态:0-待处理,1-解析中,2-导入中,3-质控中,4-成功,5-失败不能为空")
    private Integer status;

    @Schema(description = "解析状态:0-未开始,1-进行中,2-成功,3-失败", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "解析状态:0-未开始,1-进行中,2-成功,3-失败不能为空")
    private Integer parseStatus;

    @Schema(description = "导入状态:0-未开始,1-进行中,2-成功,3-失败", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "导入状态:0-未开始,1-进行中,2-成功,3-失败不能为空")
    private Integer importStatus;

    @Schema(description = "质控状态:0-未开始,1-进行中,2-成功,3-失败", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "质控状态:0-未开始,1-进行中,2-成功,3-失败不能为空")
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

    @Schema(description = "处理进度百分比", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "处理进度百分比不能为空")
    private BigDecimal progressPercent;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "解析完成时间")
    private LocalDateTime parseEndTime;

    @Schema(description = "导入完成时间")
    private LocalDateTime importEndTime;

    @Schema(description = "质控完成时间")
    private LocalDateTime qcEndTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "错误行详情(JSON格式)")
    private String errorRowsDetail;

    @Schema(description = "导入批次号")
    private String importBatchNo;

    @Schema(description = "重试次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "14394")
    @NotNull(message = "重试次数不能为空")
    private Integer retryCount;

    @Schema(description = "最大重试次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "14274")
    @NotNull(message = "最大重试次数不能为空")
    private Integer maxRetryCount;

}