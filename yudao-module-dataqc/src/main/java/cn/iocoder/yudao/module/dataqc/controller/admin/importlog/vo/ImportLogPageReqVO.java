package cn.iocoder.yudao.module.dataqc.controller.admin.importlog.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 数据导入日志分页 Request VO")
@Data
public class ImportLogPageReqVO extends PageParam {

    @Schema(description = "批次号")
    private String batchNo;

    @Schema(description = "文件名", example = "李四")
    private String fileName;

    @Schema(description = "文件类型", example = "2")
    private String fileType;

    @Schema(description = "目标表名", example = "张三")
    private String tableName;

    @Schema(description = "总行数")
    private Integer totalRows;

    @Schema(description = "成功行数")
    private Integer successRows;

    @Schema(description = "失败行数")
    private Integer failRows;

    @Schema(description = "状态(PROCESSING-处理中,SUCCESS-成功,FAIL-失败)", example = "1")
    private String status;

    @Schema(description = "错误信息")
    private String errorMsg;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] startTime;

    @Schema(description = "结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] endTime;

    @Schema(description = "操作人ID", example = "3771")
    private Long operatorId;

    @Schema(description = "操作人姓名", example = "赵六")
    private String operatorName;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}