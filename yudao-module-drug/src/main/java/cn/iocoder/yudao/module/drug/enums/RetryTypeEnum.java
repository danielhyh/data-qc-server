// ==================== 重试类型枚举 ====================
package cn.iocoder.yudao.module.drug.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 重试类型枚举
 * 设计理念：支持精确的重试策略，提高重试成功率
 */
@Getter
@AllArgsConstructor
public enum RetryTypeEnum {
    
    ALL("ALL", "全部重试", "重新执行整个导入流程"),
    FAILED("FAILED", "仅失败部分", "只重试之前失败的表和记录"),
    FILE_TYPE("FILE_TYPE", "指定文件类型", "重试指定类型的文件"),
    STAGE("STAGE", "指定阶段", "重试指定的处理阶段"),
    SMART("SMART", "智能重试", "基于失败原因智能选择重试策略");
    
    private final String code;
    private final String displayName;
    private final String description;
    
    /**
     * 根据代码获取枚举
     */
    public static RetryTypeEnum getByCode(String code) {
        for (RetryTypeEnum retryType : values()) {
            if (retryType.getCode().equals(code)) {
                return retryType;
            }
        }
        return null;
    }
}