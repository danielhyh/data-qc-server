package cn.iocoder.yudao.module.drug.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DetailStatusEnum {
    
    PENDING(0, "待处理"),
    PARSING(1, "解析中"),
    IMPORTING(2, "导入中"),
    QC_CHECKING(3, "质控中"),
    SUCCESS(4, "成功"),
    FAILED(5, "失败");
    
    private final Integer status;
    private final String description;
}