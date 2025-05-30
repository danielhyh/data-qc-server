package cn.iocoder.yudao.module.dataqc.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.data.ReadCellData;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Excel原始数据读取工具类
 * 用于调试Excel文件内容和结构
 */
public class ExcelRawDataReader {

    /**
     * 读取并打印Excel文件的原始数据
     * @param file 上传的Excel文件
     */
    public static void readAndPrintRawData(MultipartFile file) {
        try {
            System.out.println("=== 开始读取Excel原始数据 ===");
            System.out.println("文件名: " + file.getOriginalFilename());
            System.out.println("文件大小: " + file.getSize() + " bytes");
            System.out.println();

            EasyExcel.read(file.getInputStream(), new AnalysisEventListener<Map<Integer, String>>() {
                private int currentRowIndex = 0;

                @Override
                public void invoke(Map<Integer, String> data, AnalysisContext context) {
                    currentRowIndex++;
                    System.out.println("第" + currentRowIndex + "行数据:");
                    
                    // 打印每一列的数据
                    for (Map.Entry<Integer, String> entry : data.entrySet()) {
                        Integer columnIndex = entry.getKey();
                        String cellValue = entry.getValue();
                        System.out.println("  列" + columnIndex + ": [" + cellValue + "]");
                    }
                    System.out.println("  --------");
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    System.out.println("=== Excel数据读取完成 ===");
                    System.out.println("总共读取了 " + currentRowIndex + " 行数据");
                }

                @Override
                public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                    System.out.println("=== 表头信息 ===");
                    for (Map.Entry<Integer, String> entry : headMap.entrySet()) {
                        System.out.println("列" + entry.getKey() + " 表头: [" + entry.getValue() + "]");
                    }
                    System.out.println();
                }
            }).sheet().doRead();

        } catch (Exception e) {
            System.err.println("读取Excel文件时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 更详细的原始数据读取，包含单元格格式信息
     * @param file 上传的Excel文件
     */
    public static void readDetailedRawData(MultipartFile file) {
        try {
            System.out.println("=== 开始详细读取Excel原始数据 ===");

            EasyExcel.read(file.getInputStream(), new AnalysisEventListener<Map<Integer, ReadCellData<?>>>() {
                private int currentRowIndex = 0;

                @Override
                public void invoke(Map<Integer, ReadCellData<?>> data, AnalysisContext context) {
                    currentRowIndex++;
                    System.out.println("第" + currentRowIndex + "行详细数据:");
                    
                    for (Map.Entry<Integer, ReadCellData<?>> entry : data.entrySet()) {
                        Integer columnIndex = entry.getKey();
                        ReadCellData<?> cellData = entry.getValue();
                        
                        System.out.println("  列" + columnIndex + ":");
                        System.out.println("    字符串值: [" + cellData.getStringValue() + "]");
                        System.out.println("    数据值: [" + cellData.getData() + "]");
                        System.out.println("    数据类型: " + cellData.getType());
                        if (cellData.getNumberValue() != null) {
                            System.out.println("    数字值: " + cellData.getNumberValue());
                        }
                    }
                    System.out.println("  --------");
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    System.out.println("=== 详细Excel数据读取完成 ===");
                }
            }).sheet().doRead();

        } catch (Exception e) {
            System.err.println("详细读取Excel文件时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 读取指定行范围的数据
     * @param file Excel文件
     * @param startRow 开始行号（从1开始）
     * @param endRow 结束行号
     */
    public static void readSpecificRows(MultipartFile file, int startRow, int endRow) {
        try {
            System.out.println("=== 读取第" + startRow + "行到第" + endRow + "行的数据 ===");

            EasyExcel.read(file.getInputStream(), new AnalysisEventListener<Map<Integer, String>>() {
                private int currentRowIndex = 0;

                @Override
                public void invoke(Map<Integer, String> data, AnalysisContext context) {
                    currentRowIndex++;
                    
                    // 只处理指定范围内的行
                    if (currentRowIndex >= startRow && currentRowIndex <= endRow) {
                        System.out.println("第" + currentRowIndex + "行数据:");
                        for (Map.Entry<Integer, String> entry : data.entrySet()) {
                            System.out.println("  列" + entry.getKey() + ": [" + entry.getValue() + "]");
                        }
                        System.out.println("  --------");
                    }
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    System.out.println("=== 指定范围数据读取完成 ===");
                }
            }).sheet().doRead();

        } catch (Exception e) {
            System.err.println("读取指定范围数据时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}