package cn.iocoder.yudao.module.drug.enums;

import cn.hutool.core.util.ArrayUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileTypeEnum {
    
    HOSPITAL_INFO("HOSPITAL_INFO", "机构基本情况.xlsx", TableTypeEnum.HOSPITAL_INFO),
    DRUG_CATALOG("DRUG_CATALOG", "药品目录.xlsx", TableTypeEnum.DRUG_CATALOG),
    DRUG_INBOUND("DRUG_INBOUND", "入库情况.xlsx", TableTypeEnum.DRUG_INBOUND),
    DRUG_OUTBOUND("DRUG_OUTBOUND", "出库情况.xlsx", TableTypeEnum.DRUG_OUTBOUND),
    DRUG_USAGE("DRUG_USAGE", "使用情况.xlsx", TableTypeEnum.DRUG_USAGE);
    
    private final String fileType;
    private final String fileName;
    private final TableTypeEnum tableType;
    
    /**
     * 根据文件名获取文件类型
     */
    public static FileTypeEnum getByFileName(String fileName) {
        return ArrayUtil.firstMatch(fileEnum ->
            fileName.contains(fileEnum.getFileName().replace(".xlsx", "")), values());
    }
}