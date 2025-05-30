package cn.iocoder.yudao.module.drug.controller.admin.batch.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 药品数据导入任务明细 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ImportTaskDetailRespVO {

    @Schema(description = "明细ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1835")
    @ExcelProperty("明细ID")
    private Long id;

    @Schema(description = "任务ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "11474")
    @ExcelProperty("任务ID")
    private Long taskId;

    @Schema(description = "任务编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("任务编号")
    private String taskNo;

    @Schema(description = "文件类型:HOSPITAL_INFO,DRUG_CATALOG,DRUG_INBOUND,DRUG_OUTBOUND,DRUG_USAGE", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("文件类型:HOSPITAL_INFO,DRUG_CATALOG,DRUG_INBOUND,DRUG_OUTBOUND,DRUG_USAGE")
    private String fileType;

    @Schema(description = "文件名", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @ExcelProperty("文件名")
    private String fileName;

    @Schema(description = "目标表名", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("目标表名")
    private String targetTable;

    @Schema(description = "表类型:1-机构信息,2-药品目录,3-入库情况,4-出库情况,5-使用情况", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("表类型:1-机构信息,2-药品目录,3-入库情况,4-出库情况,5-使用情况")
    private Integer tableType;

    @Schema(description = "状态:0-待处理,1-解析中,2-导入中,3-质控中,4-成功,5-失败", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("状态:0-待处理,1-解析中,2-导入中,3-质控中,4-成功,5-失败")
    private Integer status;

    @Schema(description = "解析状态:0-未开始,1-进行中,2-成功,3-失败", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("解析状态:0-未开始,1-进行中,2-成功,3-失败")
    private Integer parseStatus;

    @Schema(description = "导入状态:0-未开始,1-进行中,2-成功,3-失败", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("导入状态:0-未开始,1-进行中,2-成功,3-失败")
    private Integer importStatus;

    @Schema(description = "质控状态:0-未开始,1-进行中,2-成功,3-失败", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("质控状态:0-未开始,1-进行中,2-成功,3-失败")
    private Integer qcStatus;

    @Schema(description = "总行数")
    @ExcelProperty("总行数")
    private Long totalRows;

    @Schema(description = "有效行数")
    @ExcelProperty("有效行数")
    private Long validRows;

    @Schema(description = "导入成功行数")
    @ExcelProperty("导入成功行数")
    private Long successRows;

    @Schema(description = "导入失败行数")
    @ExcelProperty("导入失败行数")
    private Long failedRows;

    @Schema(description = "质控通过行数")
    @ExcelProperty("质控通过行数")
    private Long qcPassedRows;

    @Schema(description = "质控失败行数")
    @ExcelProperty("质控失败行数")
    private Long qcFailedRows;

    @Schema(description = "处理进度百分比", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("处理进度百分比")
    private BigDecimal progressPercent;

    @Schema(description = "开始时间")
    @ExcelProperty("开始时间")
    private LocalDateTime startTime;

    @Schema(description = "解析完成时间")
    @ExcelProperty("解析完成时间")
    private LocalDateTime parseEndTime;

    @Schema(description = "导入完成时间")
    @ExcelProperty("导入完成时间")
    private LocalDateTime importEndTime;

    @Schema(description = "质控完成时间")
    @ExcelProperty("质控完成时间")
    private LocalDateTime qcEndTime;

    @Schema(description = "结束时间")
    @ExcelProperty("结束时间")
    private LocalDateTime endTime;

    @Schema(description = "错误信息")
    @ExcelProperty("错误信息")
    private String errorMessage;

    @Schema(description = "错误行详情(JSON格式)")
    @ExcelProperty("错误行详情(JSON格式)")
    private String errorRowsDetail;

    @Schema(description = "导入批次号")
    @ExcelProperty("导入批次号")
    private String importBatchNo;

    @Schema(description = "重试次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "14394")
    @ExcelProperty("重试次数")
    private Integer retryCount;

    @Schema(description = "最大重试次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "14274")
    @ExcelProperty("最大重试次数")
    private Integer maxRetryCount;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}