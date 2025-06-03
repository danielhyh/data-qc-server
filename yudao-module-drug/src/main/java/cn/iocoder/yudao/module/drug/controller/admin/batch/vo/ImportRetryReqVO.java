package cn.iocoder.yudao.module.drug.controller.admin.batch.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 导入重试入参
 */
@Data
@Builder
public class ImportRetryReqVO {

    private Long taskId;
    private String retryType;             // 重试类型
    private String fileType;              // 文件类型（当retryType为FILE_TYPE时必填）
}