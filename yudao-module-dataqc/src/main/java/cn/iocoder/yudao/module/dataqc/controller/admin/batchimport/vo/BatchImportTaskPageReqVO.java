package cn.iocoder.yudao.module.dataqc.controller.admin.batchimport.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 批量导入任务分页 Request VO")
@Data
public class BatchImportTaskPageReqVO extends PageParam {

    @Schema(description = "任务编号")
    private String taskNo;

    @Schema(description = "任务名称", example = "李四")
    private String taskName;

    @Schema(description = "上传文件名", example = "张三")
    private String fileName;

    @Schema(description = "文件存储路径")
    private String filePath;

    @Schema(description = "状态(0-待处理,1-处理中,2-成功,3-失败,4-部分成功)", example = "1")
    private Integer status;

    @Schema(description = "文件总数")
    private Integer totalFiles;

    @Schema(description = "成功文件数")
    private Integer successFiles;

    @Schema(description = "失败文件数")
    private Integer failFiles;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] startTime;

    @Schema(description = "结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] endTime;

    @Schema(description = "错误信息")
    private String errorMsg;

    @Schema(description = "结果详情(JSON格式)")
    private String resultDetail;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}