package cn.iocoder.yudao.module.dataqc.controller.admin.batchimport.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 导入进度响应VO
 * 
 * 设计说明：
 * 这个VO专门用于返回导入进度信息
 * 包含了前端进度条显示所需的所有数据
 */
@Data
public class BatchImportProgressVO {
    
    @Schema(description = "任务ID")
    private Long taskId;
    
    @Schema(description = "任务编号")
    private String taskNo;
    
    @Schema(description = "任务状态：0-待处理，1-处理中，2-成功，3-失败，4-部分成功")
    private Integer status;
    
    @Schema(description = "状态描述")
    private String statusDisplay;
    
    @Schema(description = "总文件数")
    private Integer totalFiles;
    
    @Schema(description = "成功文件数")
    private Integer successFiles;
    
    @Schema(description = "失败文件数")
    private Integer failFiles;
    
    @Schema(description = "进度百分比")
    private Integer progressPercentage;
    
    @Schema(description = "开始时间")
    private LocalDateTime startTime;
    
    @Schema(description = "预计剩余时间（秒）")
    private Long estimatedRemainingTime;
    
    @Schema(description = "处理明细")
    private List<BatchImportTaskDetailRespVO> details;
    
    // 获取状态显示文本
    public String getStatusDisplay() {
        if (status == null) return "未知";
        
        switch (status) {
            case 0: return "待处理";
            case 1: return "处理中";
            case 2: return "成功";
            case 3: return "失败";
            case 4: return "部分成功";
            case 5: return "已取消";
            default: return "未知";
        }
    }
}