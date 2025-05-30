package cn.iocoder.yudao.framework.excel.core.util;

import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.framework.excel.core.handler.SelectSheetWriteHandler;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.converters.longconverter.LongStringConverter;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Excel 工具类 - 增强版
 * <p>
 * 支持功能：
 * - 自定义表头行号和数据起始行
 * - 流式读取大文件
 * - 数据校验和转换
 * - 批量处理
 * - 错误处理和日志记录
 *
 * @author 芋道源码
 */
@Slf4j
public class ExcelUtils {

    /**
     * 将列表以 Excel 响应给前端
     */
    public static <T> void write(HttpServletResponse response, String filename, String sheetName,
                                 Class<T> head, List<T> data) throws IOException {
        // 输出 Excel
        EasyExcel.write(response.getOutputStream(), head)
                .autoCloseStream(false) // 不要自动关闭，交给 Servlet 自己处理
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()) // 基于 column 长度，自动适配。最大 255 宽度
                .registerWriteHandler(new SelectSheetWriteHandler(head)) // 基于固定 sheet 实现下拉框
                .registerConverter(new LongStringConverter()) // 避免 Long 类型丢失精度
                .sheet(sheetName).doWrite(data);
        // 设置 header 和 contentType。写在最后的原因是，避免报错时，响应 contentType 已经被修改了
        response.addHeader("Content-Disposition", "attachment;filename=" + HttpUtils.encodeUtf8(filename));
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
    }

    // ==================== 基础读取方法 ====================

    /**
     * 读取Excel文件 - 基础版本（保持原有兼容性）
     */
    public static <T> List<T> read(MultipartFile file, Class<T> head) throws IOException {
        return read(file, head, 0, 1);
    }

    /**
     * 读取Excel文件 - 指定表头行号
     *
     * @param file          上传文件
     * @param head          数据类型
     * @param headRowNumber 表头行号（从0开始）
     * @return 解析后的数据列表
     */
    public static <T> List<T> read(MultipartFile file, Class<T> head, int headRowNumber) throws IOException {
        return read(file, head, headRowNumber, headRowNumber + 1);
    }

    /**
     * 读取Excel文件 - 完整配置版本
     *
     * @param file          上传文件
     * @param head          数据类型
     * @param headRowNumber 表头行号（从0开始，如第3行表头则传2）
     * @param dataStartRow  数据起始行号（从0开始，如第4行开始数据则传3）
     * @return 解析后的数据列表
     */
    public static <T> List<T> read(MultipartFile file, Class<T> head,
                                   int headRowNumber, int dataStartRow) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            return EasyExcel.read(inputStream, head, null)
                    .autoCloseStream(false)
                    .headRowNumber(headRowNumber)
                    .sheet()
                    .doReadSync();
        } catch (Exception e) {
            log.error("Excel文件读取失败，文件名：{}", file.getOriginalFilename(), e);
            throw new IOException("Excel文件读取失败：" + e.getMessage(), e);
        }
    }

    /**
     * 读取Excel文件 - 带监听器版本
     *
     * @param file          上传文件
     * @param head          数据类型
     * @param listener      读取监听器
     * @param headRowNumber 表头行号
     * @param dataStartRow  数据起始行号
     * @return 解析后的数据列表
     */
    public static <T> List<T> read(MultipartFile file, Class<T> head,
                                   ReadListener<T> listener,
                                   int headRowNumber, int dataStartRow) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            return EasyExcel.read(inputStream, head, listener)
                    .autoCloseStream(false)
                    .headRowNumber(headRowNumber)
                    .sheet()
                    .doReadSync();
        } catch (Exception e) {
            log.error("Excel文件读取失败，文件名：{}", file.getOriginalFilename(), e);
            throw new IOException("Excel文件读取失败：" + e.getMessage(), e);
        }
    }

    // ==================== 流式读取方法（大文件处理） ====================

    /**
     * 流式读取Excel文件 - 批量处理版本
     * 适用于大文件，避免内存溢出
     *
     * @param file           上传文件
     * @param head           数据类型
     * @param batchProcessor 批量处理器
     * @param batchSize      每批处理数量
     * @param headRowNumber  表头行号
     */
    public static <T> void readInBatch(MultipartFile file, Class<T> head,
                                       Consumer<List<T>> batchProcessor,
                                       int batchSize, int headRowNumber) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            EasyExcel.read(inputStream, head, new PageReadListener<T>(batchProcessor, batchSize))
                    .autoCloseStream(false)
                    .headRowNumber(headRowNumber)
                    .sheet()
                    .doRead();
        } catch (Exception e) {
            log.error("Excel文件流式读取失败，文件名：{}", file.getOriginalFilename(), e);
            throw new IOException("Excel文件流式读取失败：" + e.getMessage(), e);
        }
    }

    /**
     * 流式读取Excel文件 - 带数据验证和转换
     *
     * @param file           上传文件
     * @param head           数据类型
     * @param validator      数据验证器
     * @param converter      数据转换器
     * @param batchProcessor 批量处理器
     * @param batchSize      每批处理数量
     * @param headRowNumber  表头行号
     */
    public static <T, R> void readWithValidation(MultipartFile file, Class<T> head,
                                                 Function<T, Boolean> validator,
                                                 Function<T, R> converter,
                                                 Consumer<List<R>> batchProcessor,
                                                 int batchSize, int headRowNumber) throws IOException {

        List<R> batch = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream()) {
            EasyExcel.read(inputStream, head, new ReadListener<T>() {
                        @Override
                        public void invoke(T data, AnalysisContext context) {
                            try {
                                // 数据校验
                                if (validator.apply(data)) {
                                    // 数据转换
                                    R convertedData = converter.apply(data);
                                    batch.add(convertedData);

                                    // 达到批次大小时处理
                                    if (batch.size() >= batchSize) {
                                        batchProcessor.accept(new ArrayList<>(batch));
                                        batch.clear();
                                    }
                                } else {
                                    log.warn("数据校验失败，行号：{}，数据：{}", context.readRowHolder().getRowIndex(), data);
                                }
                            } catch (Exception e) {
                                log.error("数据处理异常，行号：{}，数据：{}", context.readRowHolder().getRowIndex(), data, e);
                            }
                        }

                        @Override
                        public void doAfterAllAnalysed(AnalysisContext context) {
                            // 处理最后一批数据
                            if (!batch.isEmpty()) {
                                batchProcessor.accept(batch);
                                batch.clear();
                            }
                            log.info("Excel文件读取完成，总行数：{}", context.readRowHolder().getRowIndex());
                        }
                    })
                    .autoCloseStream(false)
                    .headRowNumber(headRowNumber)
                    .sheet()
                    .doRead();

        } catch (Exception e) {
            log.error("Excel文件验证读取失败，文件名：{}", file.getOriginalFilename(), e);
            throw new IOException("Excel文件验证读取失败：" + e.getMessage(), e);
        }
    }

    // ==================== 建造者模式配置 ====================

    /**
     * 创建Excel读取构建器
     */
    public static <T> ExcelReaderBuilder<T> reader(MultipartFile file, Class<T> head) {
        return new ExcelReaderBuilder<>(file, head);
    }

    /**
     * 验证Excel文件格式
     */
    public static void validateExcelFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null ||
                (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls"))) {
            throw new IOException("文件格式不正确，只支持.xlsx和.xls格式");
        }

        // 文件大小检查 (50MB)
        if (file.getSize() > 50 * 1024 * 1024) {
            throw new IOException("文件大小不能超过50MB");
        }
    }

    /**
     * 获取Excel文件基本信息
     */
    public static ExcelFileInfo getFileInfo(MultipartFile file) throws IOException {
        validateExcelFile(file);

        try (InputStream inputStream = file.getInputStream()) {
            // 简单读取第一行来获取列数等信息
            var reader = EasyExcel.read(inputStream).sheet(0);
            // 这里可以扩展更多文件信息获取逻辑

            return ExcelFileInfo.builder()
                    .fileName(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .build();

        } catch (Exception e) {
            log.error("获取Excel文件信息失败，文件名：{}", file.getOriginalFilename(), e);
            throw new IOException("获取Excel文件信息失败：" + e.getMessage(), e);
        }
    }

    /**
     * Excel读取构建器 - 建造者模式
     */
    public static class ExcelReaderBuilder<T> {
        private final MultipartFile file;
        private final Class<T> head;
        private int headRowNumber = 0;
        private int sheetNo = 0;
        private String sheetName;
        private ReadListener<T> listener;
        private Function<T, Boolean> validator;
        private int batchSize = 1000;

        public ExcelReaderBuilder(MultipartFile file, Class<T> head) {
            this.file = file;
            this.head = head;
        }

        /**
         * 设置表头行号（从0开始）
         */
        public ExcelReaderBuilder<T> headRowNumber(int headRowNumber) {
            this.headRowNumber = headRowNumber;
            return this;
        }

        /**
         * 设置Sheet索引
         */
        public ExcelReaderBuilder<T> sheet(int sheetNo) {
            this.sheetNo = sheetNo;
            return this;
        }

        /**
         * 设置Sheet名称
         */
        public ExcelReaderBuilder<T> sheet(String sheetName) {
            this.sheetName = sheetName;
            return this;
        }

        /**
         * 设置读取监听器
         */
        public ExcelReaderBuilder<T> listener(ReadListener<T> listener) {
            this.listener = listener;
            return this;
        }

        /**
         * 设置数据验证器
         */
        public ExcelReaderBuilder<T> validator(Function<T, Boolean> validator) {
            this.validator = validator;
            return this;
        }

        /**
         * 设置批量处理大小
         */
        public ExcelReaderBuilder<T> batchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        /**
         * 批量流式读取
         */
        public void doReadInBatch(Consumer<List<T>> batchProcessor) throws IOException {
            Consumer<List<T>> processedBatchProcessor = batchProcessor;

            // 如果有验证器，先过滤再处理
            if (validator != null) {
                processedBatchProcessor = batch -> {
                    List<T> validData = batch.stream()
                            .filter(validator::apply)
                            .toList();
                    if (!validData.isEmpty()) {
                        batchProcessor.accept(validData);
                    }
                };
            }

            readInBatch(file, head, processedBatchProcessor, batchSize, headRowNumber);
        }
    }

    /**
     * Excel文件信息
     */
    public static class ExcelFileInfo {
        private String fileName;
        private long fileSize;
        private int sheetCount;
        private int totalRows;
        private int columnCount;

        // 建造者模式
        public static ExcelFileInfoBuilder builder() {
            return new ExcelFileInfoBuilder();
        }

        // Getters
        public String getFileName() {
            return fileName;
        }

        public long getFileSize() {
            return fileSize;
        }

        public int getSheetCount() {
            return sheetCount;
        }

        public int getTotalRows() {
            return totalRows;
        }

        public int getColumnCount() {
            return columnCount;
        }

        public static class ExcelFileInfoBuilder {
            private String fileName;
            private long fileSize;
            private int sheetCount;
            private int totalRows;
            private int columnCount;

            public ExcelFileInfoBuilder fileName(String fileName) {
                this.fileName = fileName;
                return this;
            }

            public ExcelFileInfoBuilder fileSize(long fileSize) {
                this.fileSize = fileSize;
                return this;
            }

            public ExcelFileInfoBuilder sheetCount(int sheetCount) {
                this.sheetCount = sheetCount;
                return this;
            }

            public ExcelFileInfoBuilder totalRows(int totalRows) {
                this.totalRows = totalRows;
                return this;
            }

            public ExcelFileInfoBuilder columnCount(int columnCount) {
                this.columnCount = columnCount;
                return this;
            }

            public ExcelFileInfo build() {
                ExcelFileInfo info = new ExcelFileInfo();
                info.fileName = this.fileName;
                info.fileSize = this.fileSize;
                info.sheetCount = this.sheetCount;
                info.totalRows = this.totalRows;
                info.columnCount = this.columnCount;
                return info;
            }
        }
    }
}