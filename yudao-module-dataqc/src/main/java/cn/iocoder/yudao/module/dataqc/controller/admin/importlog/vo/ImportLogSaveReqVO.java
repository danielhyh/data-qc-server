package cn.iocoder.yudao.module.dataqc.controller.admin.importlog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 数据导入日志新增/修改 Request VO")
@Data
public class ImportLogSaveReqVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10086")
    private Long id;

    @Schema(description = "批次号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "批次号不能为空")
    private String batchNo;

    @Schema(description = "文件名", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotEmpty(message = "文件名不能为空")
    private String fileName;

    @Schema(description = "文件类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "文件类型不能为空")
    private String fileType;

    @Schema(description = "目标表名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotEmpty(message = "目标表名不能为空")
    private String tableName;

    @Schema(description = "总行数")
    private Integer totalRows;

    @Schema(description = "成功行数")
    private Integer successRows;

    @Schema(description = "失败行数")
    private Integer failRows;

    @Schema(description = "状态(PROCESSING-处理中,SUCCESS-成功,FAIL-失败)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "状态(PROCESSING-处理中,SUCCESS-成功,FAIL-失败)不能为空")
    private String status;

    @Schema(description = "错误信息")
    private String errorMsg;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "操作人ID", example = "3771")
    private Long operatorId;

    @Schema(description = "操作人姓名", example = "赵六")
    private String operatorName;

}