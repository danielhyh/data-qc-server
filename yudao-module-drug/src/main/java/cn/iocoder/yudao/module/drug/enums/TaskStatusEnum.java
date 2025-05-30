package cn.iocoder.yudao.module.drug.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskStatusEnum {
    
    PENDING(0, "待处理"),
    EXTRACTING(1, "解压中"),
    IMPORTING(2, "数据导入中"),
    QC_CHECKING(3, "质控中"),
    COMPLETED(4, "完成"),
    FAILED(5, "失败"),
    PARTIAL_SUCCESS(6, "部分成功");
    
    private final Integer status;
    private final String description;
    
    /**
     * 判断是否为进行中状态
     */
    public boolean isProcessing() {
        return this == EXTRACTING || this == IMPORTING || this == QC_CHECKING;
    }
    
    /**
     * 判断是否为最终状态
     */
    public boolean isFinalStatus() {
        return this == COMPLETED || this == FAILED || this == PARTIAL_SUCCESS;
    }
}