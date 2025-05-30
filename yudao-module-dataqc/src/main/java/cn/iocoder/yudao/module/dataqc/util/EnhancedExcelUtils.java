package cn.iocoder.yudao.module.dataqc.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.builder.ExcelReaderSheetBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 增强的Excel工具类
 * 提供更灵活的Excel读取功能，支持自定义转换器
 * 
 * @author 系统管理员
 */
@Slf4j
public class EnhancedExcelUtils {
    
    /**
     * 使用自定义监听器读取Excel（适合大文件）
     * 
     * @param file Excel文件
     * @param clazz 数据类型
     * @param listener 自定义监听器
     * @param <T> 数据类型泛型
     * @return 读取的数据列表
     */
    public static <T> List<T> readWithListener(MultipartFile file, Class<T> clazz, 
                                               ExcelReadListener<T> listener) {
        try (InputStream inputStream = file.getInputStream()) {
            // 构建读取器
            ExcelReaderBuilder readerBuilder = EasyExcel.read(inputStream, clazz, listener);
            
            // 添加自定义转换器
            for (Converter<?> converter : listener.getConverters()) {
                readerBuilder.registerConverter(converter);
            }
            
            // 执行读取
            readerBuilder.sheet().doRead();
            
            // 返回读取的数据
            return listener.getDataList();
            
        } catch (IOException e) {
            log.error("读取Excel文件失败：{}", file.getOriginalFilename(), e);
            throw new RuntimeException("读取Excel文件失败", e);
        }
    }
    
    /**
     * 同步读取Excel（适合小文件）
     * 支持自定义转换器
     * 
     * @param file Excel文件
     * @param clazz 数据类型
     * @param converters 自定义转换器列表
     * @param <T> 数据类型泛型
     * @return 读取的数据列表
     */
    public static <T> List<T> readSync(MultipartFile file, Class<T> clazz, 
                                       Converter<?>... converters) {
        try (InputStream inputStream = file.getInputStream()) {
            // 构建读取器
            ExcelReaderSheetBuilder sheetBuilder = EasyExcel.read(inputStream, clazz, new ExcelReadListener<T>())
                    .sheet();
            
            // 注册自定义转换器
            for (Converter<?> converter : converters) {
                sheetBuilder.registerConverter(converter);
            }
            
            // 同步读取所有数据
            return sheetBuilder.doReadSync();
            
        } catch (IOException e) {
            log.error("读取Excel文件失败：{}", file.getOriginalFilename(), e);
            throw new RuntimeException("读取Excel文件失败", e);
        }
    }
    
    /**
     * 使用默认配置读取Excel
     * 包含常用的数据转换器
     * 
     * @param file Excel文件
     * @param clazz 数据类型
     * @param <T> 数据类型泛型
     * @return 读取的数据列表
     */
    public static <T> List<T> readWithDefaultConverters(MultipartFile file, Class<T> clazz) {
        // 创建监听器并添加默认转换器
        ExcelReadListener<T> listener = new ExcelReadListener<T>()
                .addConverter(new ExcelDataConverter.SafeLongConverter())
                .addConverter(new ExcelDataConverter.SafeBigDecimalConverter());
        
        return readWithListener(file, clazz, listener);
    }
    
    /**
     * 读取Excel指定Sheet
     * 
     * @param file Excel文件
     * @param clazz 数据类型
     * @param sheetNo Sheet编号（从0开始）
     * @param converters 自定义转换器
     * @param <T> 数据类型泛型
     * @return 读取的数据列表
     */
    public static <T> List<T> readSheet(MultipartFile file, Class<T> clazz, 
                                        Integer sheetNo, Converter<?>... converters) {
        try (InputStream inputStream = file.getInputStream()) {
            ExcelReaderBuilder readerBuilder = EasyExcel.read(inputStream, clazz, new ExcelReadListener<T>());
            
            // 注册转换器
            for (Converter<?> converter : converters) {
                readerBuilder.registerConverter(converter);
            }
            
            // 读取指定Sheet
            return readerBuilder.sheet(sheetNo).doReadSync();
            
        } catch (IOException e) {
            log.error("读取Excel文件失败：{}", file.getOriginalFilename(), e);
            throw new RuntimeException("读取Excel文件失败", e);
        }
    }
}