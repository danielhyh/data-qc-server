package cn.iocoder.yudao.module.dataqc.util;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.event.AnalysisEventListener;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Excel读取监听器
 * 用于处理Excel数据读取过程中的事件和数据转换
 * 
 * @param <T> 数据对象类型
 * @author 系统管理员
 */
@Slf4j
public class ExcelReadListener<T> extends AnalysisEventListener<T> {
    
    /**
     * 批量处理的阈值，每达到这个数量就处理一次
     */
    private static final int BATCH_COUNT = 1000;
    /**
     * 存储读取到的数据
     */
    private List<T> dataList = new ArrayList<>();
    /**
     * 存储自定义转换器
     */
    private List<Converter<?>> converters = new ArrayList<>();
    /**
     * 错误信息收集
     */
    private List<String> errorMessages = new ArrayList<>();
    
    /**
     * 当前处理的行号
     */
    private int currentRowNum = 0;
    
    /**
     * 构造方法
     */
    public ExcelReadListener() {
        super();
    }
    
    /**
     * 添加自定义转换器（链式调用）
     * 
     * @param converter 转换器实例
     * @return 当前监听器实例，支持链式调用
     */
    public ExcelReadListener<T> addConverter(Converter<?> converter) {
        if (converter != null) {
            this.converters.add(converter);
        }
        return this;
    }
    
    /**
     * 每解析一条数据都会调用此方法
     * 
     * @param data 解析的数据对象
     * @param context 解析上下文
     */
    @Override
    public void invoke(T data, AnalysisContext context) {
        currentRowNum = context.readRowHolder().getRowIndex() + 1;
        
        try {
            // 数据校验（可以在这里添加基础校验逻辑）
            if (data == null) {
                errorMessages.add("第" + currentRowNum + "行数据为空");
                return;
            }
            
            // 将数据添加到列表
            dataList.add(data);
            
            // 达到批量处理阈值时的处理逻辑（可选）
            if (dataList.size() >= BATCH_COUNT) {
                // 这里可以添加批量处理逻辑，比如批量保存到数据库
                log.info("已读取 {} 条数据", dataList.size());
            }
            
        } catch (Exception e) {
            log.error("处理第{}行数据时发生错误", currentRowNum, e);
            errorMessages.add("第" + currentRowNum + "行处理失败：" + e.getMessage());
        }
    }
    
    /**
     * 所有数据解析完成后会调用此方法
     * 
     * @param context 解析上下文
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("Excel解析完成，共读取 {} 条数据", dataList.size());
        
        // 处理最后剩余的数据
        if (!dataList.isEmpty()) {
            // 这里可以添加最终的数据处理逻辑
            log.info("处理最后一批数据，共 {} 条", dataList.size());
        }
        
        // 输出错误信息汇总
        if (!errorMessages.isEmpty()) {
            log.warn("Excel解析过程中发生 {} 个错误", errorMessages.size());
            errorMessages.forEach(log::warn);
        }
    }
    
    /**
     * 获取解析后的数据列表
     * 
     * @return 数据列表
     */
    public List<T> getDataList() {
        return dataList;
    }
    
    /**
     * 获取错误信息列表
     * 
     * @return 错误信息列表
     */
    public List<String> getErrorMessages() {
        return errorMessages;
    }
    
    /**
     * 获取自定义转换器列表
     * 
     * @return 转换器列表
     */
    public List<Converter<?>> getConverters() {
        return converters;
    }
    
    /**
     * 清空数据（用于重复使用监听器时）
     */
    public void clear() {
        dataList.clear();
        errorMessages.clear();
        currentRowNum = 0;
    }
    
    /**
     * 是否有错误发生
     * 
     * @return true表示有错误
     */
    public boolean hasErrors() {
        return !errorMessages.isEmpty();
    }
}