package cn.iocoder.yudao.module.drug.enums;

import cn.hutool.core.util.ArrayUtil;
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
    FAILED(5, "失败"),
    PARTIAL_SUCCESS(6, "部分成功");
    
    private final Integer status;
    private final String description;

    /**
     * 根据类型获取枚举
     */
    public static DetailStatusEnum getByStatus(Integer status) {
        return ArrayUtil.firstMatch(detailEnum -> detailEnum.getStatus().equals(status), values());
    }
}