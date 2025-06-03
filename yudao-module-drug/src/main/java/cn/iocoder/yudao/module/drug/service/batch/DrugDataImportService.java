// ==================== 数据导入服务 ====================
package cn.iocoder.yudao.module.drug.service.batch;

import cn.iocoder.yudao.module.drug.controller.admin.batch.vo.ImportResult;
import cn.iocoder.yudao.module.drug.enums.TableTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 药品数据导入服务
 * 设计理念：专注于数据持久化，支持事务管理和批量处理优化
 */
@Service
@Slf4j
public class DrugDataImportService {

    /**
     * 批量导入数据
     * 使用事务管理确保数据一致性，支持大批量数据的分批处理
     */
    @Transactional(rollbackFor = Exception.class)
    public ImportResult importBatch(Long taskId, TableTypeEnum tableType, List<?> dataList) {
        LocalDateTime startTime = LocalDateTime.now();
        String batchNo = generateBatchNo(taskId, tableType);

        try {
            log.info("开始批量导入数据: taskId={}, tableType={}, count={}",
                    taskId, tableType, dataList.size());

            // 获取对应的导入策略
            ImportStrategy strategy = getImportStrategy(tableType);

            // 执行批量导入
            ImportResult.ImportResultBuilder resultBuilder = ImportResult.builder()
                    .tableType(tableType.name())
                    .importBatchNo(batchNo)
                    .startTime(startTime)
                    .totalCount(dataList.size());

            int successCount = 0;
            int failedCount = 0;
            List<ImportResult.ImportError> errors = new ArrayList<>();

            for (int i = 0; i < dataList.size(); i++) {
                try {
                    Object data = dataList.get(i);
                    strategy.importSingle(taskId, data);
                    successCount++;
                } catch (Exception e) {
                    failedCount++;
                    errors.add(ImportResult.ImportError.builder()
                            .batchIndex(i)
                            .errorType("IMPORT_ERROR")
                            .errorMessage(e.getMessage())
                            .errorDetail(e.toString())
                            .build());
                    log.warn("单条数据导入失败: taskId={}, tableType={}, index={}",
                            taskId, tableType, i, e);
                }
            }

            LocalDateTime endTime = LocalDateTime.now();

            return resultBuilder
                    .success(failedCount == 0)
                    .hasError(failedCount > 0)
                    .message(String.format("导入完成：成功%d条，失败%d条", successCount, failedCount))
                    .successCount(successCount)
                    .failedCount(failedCount)
                    .endTime(endTime)
                    .processingTimeMs(java.time.Duration.between(startTime, endTime).toMillis())
                    .importErrors(errors)
                    .build();

        } catch (Exception e) {
            log.error("批量导入异常: taskId={}, tableType={}", taskId, tableType, e);
            return ImportResult.builder()
                    .success(false)
                    .hasError(true)
                    .message("批量导入失败: " + e.getMessage())
                    .tableType(tableType.name())
                    .importBatchNo(batchNo)
                    .startTime(startTime)
                    .endTime(LocalDateTime.now())
                    .totalCount(dataList.size())
                    .successCount(0)
                    .failedCount(dataList.size())
                    .importErrors(new ArrayList<>())
                    .build();
        }
    }

    private String generateBatchNo(Long taskId, TableTypeEnum tableType) {
        return String.format("BATCH_%d_%s_%s",
                taskId, tableType.name(), UUID.randomUUID().toString().substring(0, 8));
    }

    private ImportStrategy getImportStrategy(TableTypeEnum tableType) {
        // 工厂模式获取对应的导入策略
        return switch (tableType) {
            case HOSPITAL_INFO -> new HospitalInfoImportStrategy();
            case DRUG_CATALOG -> new DrugCatalogImportStrategy();
            case DRUG_INBOUND -> new DrugInboundImportStrategy();
            case DRUG_OUTBOUND -> new DrugOutboundImportStrategy();
            case DRUG_USAGE -> new DrugUsageImportStrategy();
        };
    }

    // 导入策略接口
    private interface ImportStrategy {
        void importSingle(Long taskId, Object data);
    }

    // 具体导入策略实现（示例）
    private static class HospitalInfoImportStrategy implements ImportStrategy {
        @Override
        public void importSingle(Long taskId, Object data) {
            // 实际导入逻辑，调用对应的Mapper
        }
    }

    // 其他策略类似实现...
    private static class DrugCatalogImportStrategy implements ImportStrategy {
        @Override
        public void importSingle(Long taskId, Object data) {
        }
    }

    private static class DrugInboundImportStrategy implements ImportStrategy {
        @Override
        public void importSingle(Long taskId, Object data) {
        }
    }

    private static class DrugOutboundImportStrategy implements ImportStrategy {
        @Override
        public void importSingle(Long taskId, Object data) {
        }
    }

    private static class DrugUsageImportStrategy implements ImportStrategy {
        @Override
        public void importSingle(Long taskId, Object data) {
        }
    }
}