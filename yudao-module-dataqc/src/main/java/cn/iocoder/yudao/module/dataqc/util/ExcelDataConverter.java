package cn.iocoder.yudao.module.dataqc.util;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/**
 * Excel数据转换器工具类
 * 解决科学计数法、大数值等转换问题
 * 
 * @author 系统管理员
 */
@Slf4j
public class ExcelDataConverter {

    /**
     * 安全的Long类型转换器
     * 专门解决科学计数法转换Long失败的问题
     */
    public static class SafeLongConverter implements Converter<Long> {
        
        @Override
        public Class<Long> supportJavaTypeKey() {
            return Long.class;
        }
        
        @Override
        public CellDataTypeEnum supportExcelTypeKey() {
            return CellDataTypeEnum.NUMBER;
        }
        
        @Override
        public Long convertToJavaData(ReadCellData<?> cellData, 
                                    ExcelContentProperty contentProperty, 
                                    GlobalConfiguration globalConfiguration) {
            try {
                // 处理空值
                if (cellData == null) {
                    return null;
                }
                
                // 处理数字类型
                if (cellData.getType() == CellDataTypeEnum.NUMBER) {
                    BigDecimal bigDecimal = cellData.getNumberValue();
                    if (bigDecimal == null) {
                        return null;
                    }
                    return bigDecimal.longValue();
                }
                
                // 处理字符串类型（可能包含科学计数法）
                String stringValue = cellData.getStringValue();
                if (StrUtil.isBlank(stringValue)) {
                    return null;
                }
                
                // 清理字符串：移除空格、逗号等
                stringValue = stringValue.trim().replace(",", "").replace(" ", "");
                
                // 处理科学计数法格式
                if (stringValue.contains("E") || stringValue.contains("e")) {
                    BigDecimal decimal = new BigDecimal(stringValue);
                    return decimal.longValue();
                }
                
                // 处理小数点（四舍五入取整）
                if (stringValue.contains(".")) {
                    BigDecimal decimal = new BigDecimal(stringValue);
                    return decimal.longValue();
                }
                
                // 普通数字转换
                return Long.valueOf(stringValue);
                
            } catch (Exception e) {
                log.warn("Long类型转换失败，原始值：{}，使用默认值0", 
                         cellData.getStringValue());
                return 0L; // 返回默认值而不是抛异常
            }
        }
    }
    
    /**
     * 安全的BigDecimal转换器
     * 用于处理金额等精度要求高的数据
     */
    public static class SafeBigDecimalConverter implements Converter<BigDecimal> {
        
        @Override
        public Class<BigDecimal> supportJavaTypeKey() {
            return BigDecimal.class;
        }
        
        @Override
        public CellDataTypeEnum supportExcelTypeKey() {
            return CellDataTypeEnum.NUMBER;
        }
        
        @Override
        public BigDecimal convertToJavaData(ReadCellData<?> cellData, 
                                          ExcelContentProperty contentProperty, 
                                          GlobalConfiguration globalConfiguration) {
            try {
                if (cellData == null) {
                    return BigDecimal.ZERO;
                }
                
                if (cellData.getType() == CellDataTypeEnum.NUMBER) {
                    BigDecimal result = cellData.getNumberValue();
                    return result != null ? result : BigDecimal.ZERO;
                }
                
                String stringValue = cellData.getStringValue();
                if (StrUtil.isBlank(stringValue)) {
                    return BigDecimal.ZERO;
                }
                
                // 清理和标准化数字格式
                stringValue = stringValue.trim()
                    .replace(",", "")
                    .replace("￥", "")
                    .replace("元", "")
                    .replace(" ", "");
                
                return new BigDecimal(stringValue);
                
            } catch (Exception e) {
                log.warn("BigDecimal转换失败，原始值：{}，使用默认值0", 
                         cellData.getStringValue());
                return BigDecimal.ZERO; // 金额字段默认为0，避免导入失败
            }
        }
    }
}