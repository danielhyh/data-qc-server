package cn.iocoder.yudao.module.dataqc.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.dataqc.controller.admin.importlog.vo.ImportLogSaveReqVO;
import cn.iocoder.yudao.module.dataqc.service.importlog.ImportLogService;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用Excel导入框架
 * 
 * 设计原则：
 * 1. 开闭原则：对扩展开放，对修改关闭
 * 2. 单一职责：每个组件只负责一件事
 * 3. 依赖倒置：依赖抽象而不是具体实现
 * 
 * @author 系统管理员
 */
@Slf4j
public class ExcelImportFramework {

    /**
     * 读取Excel数据
     * 使用通用的Map方式，避免类型转换问题
     */
    private static List<Map<Integer, Object>> readExcelData(MultipartFile file) {
        List<Map<Integer, Object>> dataList = new ArrayList<>();

        try {
            EasyExcel.read(file.getInputStream(), new AnalysisEventListener<Map<Integer, Object>>() {
                @Override
                public void invoke(Map<Integer, Object> data, AnalysisContext context) {
                    dataList.add(data);
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    log.info("Excel解析完成，共读取 {} 条数据", dataList.size());
                }

                @Override
                public void onException(Exception exception, AnalysisContext context) {
                    log.warn("第{}行读取异常：{}",
                        context.readRowHolder().getRowIndex(),
                        exception.getMessage());
                }
            }).sheet().doRead();

        } catch (Exception e) {
            log.error("读取Excel失败", e);
            throw new RuntimeException("读取Excel文件失败：" + e.getMessage());
        }

        return dataList;
    }

    /**
     * 处理数据
     * 核心处理逻辑，包括转换、校验、分类
     */
    private static <T> ProcessResult<T> processData(
            List<Map<Integer, Object>> rawDataList,
            ImportConfig<T> config,
            ImportHandler<T> handler,
            ImportContext context) {

        ProcessResult<T> result = new ProcessResult<>();
        Map<String, T> existingMap = new HashMap<>();

        for (int i = 0; i < rawDataList.size(); i++) {
            Map<Integer, Object> rowData = rawDataList.get(i);
            int rowNum = i + 2; // Excel行号从2开始（考虑表头）

            try {
                // 跳过空行
                if (config.isSkipEmptyRows() && isEmptyRow(rowData)) {
                    continue;
                }

                // 1. 数据转换
                T entity = handler.convertRow(rowData, rowNum);
                if (entity == null) {
                    result.errorMessages.add("第" + rowNum + "行：数据转换失败");
                    continue;
                }

                // 2. 数据校验
                String errorMsg = handler.validate(entity, rowNum);
                if (StrUtil.isNotEmpty(errorMsg)) {
                    result.errorMessages.add(errorMsg);
                    continue;
                }

                // 3. 判断是否存在
                String uniqueKey = handler.getUniqueKey(entity);
                T existingEntity = existingMap.computeIfAbsent(uniqueKey,
                    k -> handler.findExisting(entity));

                if (existingEntity != null) {
                    if (config.isUpdateSupport()) {
                        // 更新操作
                        handler.process(entity, true, context);
                        result.updateList.add(entity);
                    } else {
                        result.errorMessages.add("第" + rowNum + "行：数据已存在");
                    }
                } else {
                    // 新增操作
                    handler.process(entity, false, context);
                    result.insertList.add(entity);
                }

            } catch (Exception e) {
                log.error("处理第{}行数据失败", rowNum, e);
                result.errorMessages.add("第" + rowNum + "行：" + e.getMessage());
            }
        }

        return result;
    }

    /**
     * 保存数据
     */
    private static <T> void saveData(ProcessResult<T> result, ImportHandler<T> handler) {
        // 批量保存前的预处理
        if (!result.insertList.isEmpty()) {
            handler.beforeBatchSave(result.insertList);
        }
        if (!result.updateList.isEmpty()) {
            handler.beforeBatchSave(result.updateList);
        }

        // 实际的保存操作由handler实现
        // 这里只是框架层面的协调
    }

    /**
     * 工具方法：安全获取字符串值
     */
    public static String getStringValue(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString().trim();
    }

    /**
     * 工具方法：安全获取Long值
     */
    public static Long getLongValue(Object value) {
        if (value == null) {
            return null;
        }

        String strValue = value.toString().trim();
        if (strValue.isEmpty()) {
            return null;
        }

        try {
            strValue = strValue.replace(",", "").replace("，", "");

            if (strValue.contains("E") || strValue.contains("e")) {
                BigDecimal bd = new BigDecimal(strValue);
                return bd.longValue();
            }

            if (strValue.contains(".")) {
                BigDecimal bd = new BigDecimal(strValue);
                return bd.longValue();
            }

            return Long.valueOf(strValue);

        } catch (Exception e) {
            log.warn("无法将值 '{}' 转换为Long类型", value);
            return 0L;
        }
    }

    /**
     * 工具方法：安全获取BigDecimal值
     */
    public static BigDecimal getBigDecimalValue(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        String strValue = value.toString().trim();
        if (strValue.isEmpty()) {
            return BigDecimal.ZERO;
        }

        try {
            strValue = strValue.replace(",", "")
                              .replace("，", "")
                              .replace("￥", "")
                              .replace("$", "");

            return new BigDecimal(strValue);

        } catch (Exception e) {
            log.warn("无法将值 '{}' 转换为BigDecimal类型", value);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 工具方法：安全获取Integer值
     */
    public static Integer getIntegerValue(Object value) {
        Long longValue = getLongValue(value);
        if (longValue == null) {
            return null;
        }

        if (longValue > Integer.MAX_VALUE || longValue < Integer.MIN_VALUE) {
            log.warn("值 {} 超出Integer范围", longValue);
            return null;
        }

        return longValue.intValue();
    }

    /**
     * 判断是否为空行
     */
    private static boolean isEmptyRow(Map<Integer, Object> rowData) {
        return rowData.values().stream()
            .allMatch(value -> value == null || value.toString().trim().isEmpty());
    }

    /**
     * 生成批次号
     */
    private static String generateBatchNo(String prefix) {
        String timestamp = DateUtil.format(DateUtil.date(), "yyyyMMddHHmmss");
        String random = RandomUtil.randomNumbers(4);
        return prefix + "_" + timestamp + "_" + random;
    }

    /**
     * 创建导入日志
     */
    private static Long createImportLog(MultipartFile file, ImportConfig<?> config,
                                       ImportContext context, ImportLogService logService) {
        ImportLogSaveReqVO importLog = new ImportLogSaveReqVO()
            .setBatchNo(context.getBatchNo())
            .setFileName(file.getOriginalFilename())
            .setFileType(config.getFileType())
            .setTableName(config.getTableName())
            .setStatus("PROCESSING");

        return logService.createImportLog(importLog);
    }

    /**
     * 更新导入日志
     */
    private static <T> void updateImportLog(Long logId, ProcessResult<T> result,
                                           ImportLogService logService) {
        int totalRows = result.insertList.size() + result.updateList.size() + result.errorMessages.size();
        int successRows = result.insertList.size() + result.updateList.size();

        ImportLogSaveReqVO updateLog = new ImportLogSaveReqVO()
            .setId(logId)
            .setTotalRows(totalRows)
            .setSuccessRows(successRows)
            .setFailRows(result.errorMessages.size())
            .setStatus(result.errorMessages.isEmpty() ? "SUCCESS" : "PARTIAL_SUCCESS")
            .setErrorMsg(StrUtil.join("\n", result.errorMessages));

        logService.updateImportLog(updateLog);
    }

    /**
     * 更新导入日志为失败状态
     */
    private static void updateImportLogFail(Long logId, String errorMsg,
                                          ImportLogService logService) {
        logService.updateImportLogFail(logId, errorMsg);
    }

    // 私有辅助类和方法

    /**
     * 核心导入方法
     * 这是框架的主入口
     */
    @Transactional(rollbackFor = Exception.class)
    public <T> ImportResult doImport(
            MultipartFile file,
            ImportConfig<T> config,
            ImportHandler<T> handler,
            ImportLogService logService) throws Exception {

        // 1. 初始化上下文
        ImportContext context = new ImportContext();
        context.setBatchNo(generateBatchNo(config.getBatchNoPrefix()));

        // 2. 创建导入日志
        Long logId = createImportLog(file, config, context, logService);
        context.setImportLogId(logId);

        try {
            // 3. 读取Excel数据
            List<Map<Integer, Object>> rawDataList = readExcelData(file);

            if (CollUtil.isEmpty(rawDataList)) {
                throw new RuntimeException("导入文件为空");
            }

            // 4. 处理数据
            ProcessResult<T> processResult = processData(rawDataList, config, handler, context);

            // 5. 保存数据
            saveData(processResult, handler);

            // 6. 更新导入日志
            updateImportLog(logId, processResult, logService);

            // 7. 返回结果
            return ImportResult.success(
                    rawDataList.size(),
                    processResult.insertList.size(),
                    processResult.updateList.size(),
                    processResult.errorMessages.size()
            );

        } catch (Exception e) {
            log.error("导入失败", e);
            updateImportLogFail(logId, e.getMessage(), logService);
            throw e;
        }
    }

    /**
     * 导入处理器接口
     * 定义了导入过程中的扩展点
     */
    public interface ImportHandler<T> {

        /**
         * 行数据转换
         * @param rowData 原始行数据（列索引 -> 单元格值）
         * @param rowNum 行号
         * @return 转换后的实体对象
         */
        T convertRow(Map<Integer, Object> rowData, int rowNum) throws Exception;

        /**
         * 数据校验
         * @param entity 实体对象
         * @param rowNum 行号
         * @return 错误信息，null表示校验通过
         */
        String validate(T entity, int rowNum);

        /**
         * 业务处理
         * @param entity 实体对象
         * @param isUpdate 是否为更新操作
         * @param context 上下文信息
         */
        void process(T entity, boolean isUpdate, ImportContext context);

        /**
         * 判断数据是否已存在
         * @param entity 实体对象
         * @return 已存在的数据，null表示不存在
         */
        T findExisting(T entity);

        /**
         * 批量保存前的预处理（可选）
         * @param entities 实体列表
         */
        default void beforeBatchSave(List<T> entities) {
            // 默认不做处理
        }

        /**
         * 获取业务唯一键（用于判重）
         * @param entity 实体对象
         * @return 唯一键
         */
        default String getUniqueKey(T entity) {
            return String.valueOf(entity.hashCode());
        }
    }

    /**
     * Excel导入配置
     * 包含导入过程所需的所有配置信息
     */
    @Data
    public static class ImportConfig<T> {
        // 基础配置
        private String fileType;           // 文件类型标识
        private String tableName;          // 目标表名
        private String batchNoPrefix;      // 批次号前缀
        private Class<T> entityClass;      // 实体类类型

        // 功能配置
        private boolean updateSupport;     // 是否支持更新
        private boolean skipEmptyRows = true;  // 是否跳过空行
        private int batchSize = 1000;      // 批量处理大小

        // 列映射配置（可选）
        private Map<Integer, String> columnMapping;  // Excel列索引 -> 实体字段名

        // 构建器模式，方便链式调用
        public static <T> ImportConfig<T> create(Class<T> entityClass) {
            ImportConfig<T> config = new ImportConfig<>();
            config.entityClass = entityClass;
            return config;
        }

        public ImportConfig<T> fileType(String fileType) {
            this.fileType = fileType;
            return this;
        }

        public ImportConfig<T> tableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public ImportConfig<T> batchNoPrefix(String prefix) {
            this.batchNoPrefix = prefix;
            return this;
        }

        public ImportConfig<T> updateSupport(boolean support) {
            this.updateSupport = support;
            return this;
        }
    }

    /**
     * 导入上下文
     * 在整个导入过程中传递的上下文信息
     */
    @Data
    public static class ImportContext {
        private String batchNo;          // 批次号
        private Long importLogId;        // 导入日志ID
        private Map<String, Object> extras = new HashMap<>();  // 扩展信息

        public void put(String key, Object value) {
            extras.put(key, value);
        }

        @SuppressWarnings("unchecked")
        public <T> T get(String key) {
            return (T) extras.get(key);
        }
    }

    /**
     * 导入结果
     */
    @Data
    public static class ImportResult {
        private boolean success;         // 是否成功
        private int totalCount;         // 总记录数
        private int successCount;        // 成功数
        private int failCount;          // 失败数
        private int insertCount;        // 新增数
        private int updateCount;        // 更新数
        private List<String> errorMessages;  // 错误信息
        private String message;         // 结果信息

        public static ImportResult success(int total, int insert, int update, int fail) {
            ImportResult result = new ImportResult();
            result.success = true;
            result.totalCount = total;
            result.insertCount = insert;
            result.updateCount = update;
            result.failCount = fail;
            result.successCount = insert + update;
            result.message = String.format("导入成功！总数：%d，新增：%d，更新：%d，失败：%d",
                    total, insert, update, fail);
            return result;
        }

        public static ImportResult failure(String message) {
            ImportResult result = new ImportResult();
            result.success = false;
            result.message = message;
            return result;
        }
    }

    /**
     * 处理结果
     */
    @Data
    private static class ProcessResult<T> {
        private List<T> insertList = new ArrayList<>();
        private List<T> updateList = new ArrayList<>();
        private List<String> errorMessages = new ArrayList<>();
    }
}