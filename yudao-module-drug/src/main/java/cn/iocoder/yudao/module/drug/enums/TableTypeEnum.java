package cn.iocoder.yudao.module.drug.enums;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 业务表类型枚举
 */
@Getter
@AllArgsConstructor
public enum TableTypeEnum implements ArrayValuable<Integer> {
    
    HOSPITAL_INFO(1, "drug_hospital_info", "机构基本情况", "医疗机构的基础信息和规模数据"),
    DRUG_CATALOG(2, "drug_catalog", "药品目录", "医疗机构药品目录清单"),
    DRUG_INBOUND(3, "drug_inbound", "入库情况", "药品采购入库记录"),
    DRUG_OUTBOUND(4, "drug_outbound", "出库情况", "药品发放出库记录"),
    DRUG_USAGE(5, "drug_usage", "使用情况", "药品临床使用明细");
    
    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(TableTypeEnum::getType).toArray(Integer[]::new);
    
    private final Integer type;
    private final String tableName;
    private final String description;
    private final String detailDescription;
    
    /**
     * 根据类型获取枚举
     */
    public static TableTypeEnum getByType(Integer type) {
        return ArrayUtil.firstMatch(tableEnum -> tableEnum.getType().equals(type), values());
    }
    
    /**
     * 根据表名获取枚举
     */
    public static TableTypeEnum getByTableName(String tableName) {
        return ArrayUtil.firstMatch(tableEnum -> tableEnum.getTableName().equals(tableName), values());
    }
    
    @Override
    public Integer[] array() {
        return ARRAYS;
    }
}