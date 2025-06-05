package cn.iocoder.yudao.module.drug.service.batch;

import cn.iocoder.yudao.module.drug.controller.admin.batch.vo.FileExtractResult;
import cn.iocoder.yudao.module.drug.controller.admin.batch.vo.FileInfo;
import cn.iocoder.yudao.module.drug.enums.TableTypeEnum;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 文件解压和验证服务
 * <p>
 * 这个服务的设计理念就像一个细心的档案管理员：
 * 1. 首先安全地接收文件包裹（保存上传文件）
 * 2. 小心地打开包裹（解压缩文件）
 * 3. 仔细检查每个文件的身份证（文件名匹配和格式验证）
 * 4. 将文件分类整理到对应的柜子里（建立文件与表类型的映射）
 * 5. 做好详细的登记记录（构建FileExtractResult）
 * <p>
 * 这种逐步处理的方式确保了每个环节都可控，出现问题时能够快速定位。
 *
 * @author hyh
 * @since 2024-06-03
 */
@Service
@Slf4j
public class FileExtractService {

    // 支持的压缩文件格式
    private static final Set<String> SUPPORTED_ARCHIVE_EXTENSIONS = Set.of(".zip", ".rar");

    // 支持的Excel文件格式
    private static final Set<String> SUPPORTED_EXCEL_EXTENSIONS = Set.of(".xlsx", ".xls");

    // 文件名匹配模式 - 这里定义了每种业务表对应的文件名规则
    // 使用正则表达式提供灵活的匹配能力，同时保持足够的严格性
    private static final Map<TableTypeEnum, Pattern> FILE_NAME_PATTERNS = Map.of(
            TableTypeEnum.HOSPITAL_INFO, Pattern.compile(".*基本.*情况.*\\.xlsx?$", Pattern.CASE_INSENSITIVE),
            TableTypeEnum.DRUG_CATALOG, Pattern.compile(".*药品.*目录.*\\.xlsx?$", Pattern.CASE_INSENSITIVE),
            TableTypeEnum.DRUG_INBOUND, Pattern.compile(".*入库.*\\.xlsx?$", Pattern.CASE_INSENSITIVE),
            TableTypeEnum.DRUG_OUTBOUND, Pattern.compile(".*出库.*\\.xlsx?$", Pattern.CASE_INSENSITIVE),
            TableTypeEnum.DRUG_USAGE, Pattern.compile(".*使用.*\\.xlsx?$", Pattern.CASE_INSENSITIVE)
    );

    // 定义处理优先级 - 这反映了业务数据的依赖关系
    // 就像搭建房子一样，必须先打地基（机构信息），再建框架（药品目录），最后添砖加瓦（业务数据）
    private static final Map<TableTypeEnum, Integer> PROCESSING_PRIORITIES = Map.of(
            TableTypeEnum.HOSPITAL_INFO, 1,    // 最高优先级，其他数据都依赖机构信息
            TableTypeEnum.DRUG_CATALOG, 2,     // 第二优先级，业务数据需要引用药品目录
            TableTypeEnum.DRUG_INBOUND, 3,     // 业务数据可以并行处理
            TableTypeEnum.DRUG_OUTBOUND, 3,
            TableTypeEnum.DRUG_USAGE, 3
    );
    /**
     * 各表类型的必填字段定义
     * 这个配置将用于数据质量评估
     */
    private static final Map<TableTypeEnum, List<String>> REQUIRED_FIELDS_CONFIG = Map.of(
            TableTypeEnum.HOSPITAL_INFO, Arrays.asList(
                    "数据上报日期", "省级行政区划代码", "组织机构代码",
                    "医疗机构代码", "组织机构名称", "年度药品总收入（元）", "实有床位数"
            ),
            TableTypeEnum.DRUG_CATALOG, Arrays.asList(
                    "数据上报日期", "省级行政区划代码", "组织机构代码", "医疗机构代码",
                    "国家药品编码（YPID）", "院内药品唯一码", "通用名", "产品名称",
                    "批准文号", "生产企业", "制剂单位", "最小销售包装单位", "转换系数"
            ),
            TableTypeEnum.DRUG_INBOUND, Arrays.asList(
                    "数据上报日期", "省级行政区划代码", "组织机构代码", "医疗机构代码",
                    "国家药品编码（YPID）", "院内药品唯一码", "产品名称",
                    "入库总金额（元）", "入库数量（最小销售包装单位）", "入库数量（最小制剂单位）"
            ),
            TableTypeEnum.DRUG_OUTBOUND, Arrays.asList(
                    "数据上报日期", "省级行政区划代码", "组织机构代码", "医疗机构代码",
                    "国家药品编码（YPID）", "院内药品唯一码", "产品名称",
                    "出库数量（最小销售包装单位）", "出库数量（最小制剂单位）"
            ),
            TableTypeEnum.DRUG_USAGE, Arrays.asList(
                    "数据上报日期", "省级行政区划代码", "组织机构代码", "医疗机构代码",
                    "国家药品编码（YPID）", "院内药品唯一码", "产品名称",
                    "销售总金额（元）", "销售数量（最小销售包装单位）", "销售数量（最小制剂单位）"
            )
    );

    /**
     * 新的主要接口：基于文件路径的解压和验证
     * <p>
     * 这是核心方法，所有文件处理逻辑都基于已保存的文件进行。
     * 这种设计避免了MultipartFile生命周期的问题，提供了更稳定的处理能力。
     *
     * @param taskId   任务ID，用于创建工作目录
     * @param filePath 已保存的压缩文件路径
     * @return 解压和验证结果
     */
    public FileExtractResult extractAndValidateFromPath(Long taskId, String filePath) {
        LocalDateTime startTime = LocalDateTime.now();
        log.info("开始解压和验证文件: taskId={}, filePath={}", taskId, filePath);

        try {
            // 第一步：验证文件路径和基本属性
            Path sourceFile = validateFilePath(filePath);

            // 第二步：创建工作目录
            Path workDir = createWorkDirectory(taskId);

            // 第三步：解压文件
            Path extractDir = extractArchiveFile(sourceFile, workDir);

            // 第四步：扫描和验证Excel文件
            Map<TableTypeEnum, FileInfo> fileInfoMap = scanAndValidateExcelFilesWithPreview(extractDir);

            // 第五步：构建结果
            LocalDateTime endTime = LocalDateTime.now();
            long durationMs = Duration.between(startTime, endTime).toMillis();

            FileExtractResult result = FileExtractResult.builder()
                    .success(true)
                    .fileInfos(fileInfoMap)
                    .extractDurationMs(durationMs)
                    .totalFileCount(countFilesInDirectory(extractDir))
                    .validFileCount(fileInfoMap.size())
                    .extractStartTime(startTime)
                    .extractEndTime(endTime)
                    .build();

            log.info("文件解压和验证完成: taskId={}, 耗时={}ms, 有效文件数={}",
                    taskId, durationMs, fileInfoMap.size());

            return result;

        } catch (Exception e) {
            log.error("文件解压和验证失败: taskId={}, filePath={}", taskId, filePath, e);

            return FileExtractResult.builder()
                    .success(false)
                    .errorMessage("文件处理失败: " + e.getMessage())
                    .extractStartTime(startTime)
                    .extractEndTime(LocalDateTime.now())
                    .totalFileCount(0)
                    .validFileCount(0)
                    .build();
        }
    }

    /**
     * 验证文件路径的有效性
     * <p>
     * 这一步骤确保我们要处理的文件确实存在且符合基本要求。
     * 相比直接处理MultipartFile，基于文件系统的验证更加可靠和一致。
     */
    private Path validateFilePath(String filePath) throws IOException {
        if (!StringUtils.hasText(filePath)) {
            throw new IllegalArgumentException("文件路径不能为空");
        }

        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            throw new IOException("文件不存在: " + filePath);
        }

        if (!Files.isRegularFile(path)) {
            throw new IOException("路径不是有效文件: " + filePath);
        }

        long fileSize = Files.size(path);
        if (fileSize == 0) {
            throw new IOException("文件为空: " + filePath);
        }

        // 验证文件扩展名
        String fileName = path.getFileName().toString();
        String extension = getFileExtension(fileName).toLowerCase();
        if (!SUPPORTED_ARCHIVE_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("不支持的文件格式: " + extension);
        }

        log.debug("文件路径验证通过: path={}, size={}KB", filePath, fileSize / 1024);
        return path;
    }

    /**
     * 解压归档文件 - 优化版
     * <p>
     * 这个方法现在完全基于文件系统操作，避免了流处理的复杂性。
     * 同时保持了原有的安全检查和性能优化。
     */
    private Path extractArchiveFile(Path archiveFile, Path workDir) throws IOException {
        Path extractDir = workDir.resolve("extracted");
        Files.createDirectories(extractDir);

        String fileName = archiveFile.getFileName().toString();
        String fileExtension = getFileExtension(fileName);

        if (".zip".equalsIgnoreCase(fileExtension)) {
            extractZipFileFromPath(archiveFile, extractDir);
        } else if (".rar".equalsIgnoreCase(fileExtension)) {
            throw new UnsupportedOperationException("暂不支持RAR格式，请使用ZIP格式");
        } else {
            throw new IllegalArgumentException("不支持的压缩格式: " + fileExtension);
        }

        log.debug("文件解压完成: {} -> {}", archiveFile, extractDir);
        return extractDir;
    }

    /**
     * 基于文件路径解压ZIP文件
     * <p>
     * 相比原来基于MultipartFile的实现，这个版本更加稳定：
     * 1. 不会受到HTTP请求生命周期影响
     * 2. 可以重复调用（支持重试）
     * 3. 内存使用更加可控
     */
    private void extractZipFileFromPath(Path zipFile, Path extractDir) throws IOException {
        final long MAX_TOTAL_SIZE = 500 * 1024 * 1024L; // 500MB
        final int MAX_FILE_COUNT = 100;

        long totalSize = 0;
        int fileCount = 0;

        // 关键改进：直接从文件系统读取，而不是从MultipartFile流
        try (ZipInputStream zipIn = new ZipInputStream(
                new BufferedInputStream(Files.newInputStream(zipFile)),
                Charset.forName("GBK"))) {

            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                fileCount++;

                if (fileCount > MAX_FILE_COUNT) {
                    throw new IOException("压缩文件包含过多文件，最多支持 " + MAX_FILE_COUNT + " 个文件");
                }

                if (entry.isDirectory()) {
                    continue;
                }

                // 安全检查：防止路径遍历攻击
                String entryName = entry.getName();
                if (entryName.contains("..") || entryName.startsWith("/")) {
                    log.warn("跳过可疑路径的文件: {}", entryName);
                    continue;
                }

                Path targetFile = extractDir.resolve(entryName);
                Files.createDirectories(targetFile.getParent());

                // 解压文件内容
                try (OutputStream out = Files.newOutputStream(targetFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;

                    while ((bytesRead = zipIn.read(buffer)) != -1) {
                        totalSize += bytesRead;

                        if (totalSize > MAX_TOTAL_SIZE) {
                            throw new IOException("解压后文件总大小超过限制");
                        }

                        out.write(buffer, 0, bytesRead);
                    }
                }

                zipIn.closeEntry();
            }
        }

        log.info("ZIP解压完成: 文件数={}, 总大小={}KB", fileCount, totalSize / 1024);
    }

    /**
     * 工作目录管理 - 优化版
     * <p>
     * 改进了目录清理逻辑，提供更好的错误处理和日志记录
     */
    private Path createWorkDirectory(Long taskId) throws IOException {
        String baseDir = System.getProperty("java.io.tmpdir", "/tmp");
        Path workDir = Paths.get(baseDir, "drug-import", "task-" + taskId);

        // 如果目录已存在，尝试清理（支持任务重试）
        if (Files.exists(workDir)) {
            try {
                deleteDirectoryRecursively(workDir);
                log.debug("清理已存在的工作目录: {}", workDir);
            } catch (IOException e) {
                log.warn("清理工作目录失败，尝试创建新的时间戳目录: {}", e.getMessage());
                // 如果清理失败，创建带时间戳的新目录
                workDir = Paths.get(baseDir, "drug-import",
                        "task-" + taskId + "-" + System.currentTimeMillis());
            }
        }

        Files.createDirectories(workDir);
        log.debug("创建工作目录: {}", workDir.toAbsolutePath());

        return workDir;
    }

    /**
     * 递归删除目录 - 加强错误处理
     */
    private void deleteDirectoryRecursively(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return;
        }

        try {
            Files.walk(directory)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            log.warn("删除文件失败: {} - {}", path, e.getMessage());
                        }
                    });
        } catch (IOException e) {
            throw new IOException("递归删除目录失败: " + directory, e);
        }
    }

    /**
     * 根据文件名识别对应的表类型
     * <p>
     * 这个方法使用预定义的正则表达式模式来匹配文件名。
     * 这种设计的好处是既保持了足够的灵活性，又确保了匹配的准确性。
     */
    private TableTypeEnum identifyTableType(String fileName) {
        for (Map.Entry<TableTypeEnum, Pattern> entry : FILE_NAME_PATTERNS.entrySet()) {
            if (entry.getValue().matcher(fileName).matches()) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * 判断是否为Excel文件
     */
    private boolean isExcelFile(Path filePath) {
        String fileName = filePath.getFileName().toString().toLowerCase();
        return SUPPORTED_EXCEL_EXTENSIONS.stream()
                .anyMatch(fileName::endsWith);
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex) : "";
    }

    /**
     * 计算目录中的文件数量
     */
    private Integer countFilesInDirectory(Path directory) {
        try {
            return (int) Files.walk(directory)
                    .filter(Files::isRegularFile)
                    .count();
        } catch (IOException e) {
            log.warn("计算文件数量失败: {}", directory, e);
            return 0;
        }
    }

    /**
     * 扫描并验证Excel文件 - 增强版
     * <p>
     * 这个方法在原有扫描逻辑基础上，增加了深度数据解析功能
     * 它是向后兼容的，现有调用方式保持不变
     */
    private Map<TableTypeEnum, FileInfo> scanAndValidateExcelFilesWithPreview(Path extractDir) throws IOException {
        Map<TableTypeEnum, FileInfo> fileInfoMap = new HashMap<>();

        Files.walk(extractDir)
                .filter(Files::isRegularFile)
                .filter(this::isExcelFile)
                .forEach(filePath -> {
                    try {
                        TableTypeEnum tableType = identifyTableType(filePath.getFileName().toString());

                        if (tableType != null) {
                            // 关键改进：调用增强版的文件信息创建方法
                            FileInfo fileInfo = validateAndCreateFileInfoWithPreview(filePath, tableType);

                            if (fileInfo.getIsValid()) {
                                if (fileInfoMap.containsKey(tableType)) {
                                    log.warn("发现重复的表类型文件: {} 和 {}",
                                            fileInfoMap.get(tableType).getFileName(),
                                            fileInfo.getFileName());
                                } else {
                                    fileInfoMap.put(tableType, fileInfo);
                                    log.info("识别到有效文件: {} -> {}, 实际字段数: {}",
                                            fileInfo.getFileName(), tableType.getDescription(),
                                            fileInfo.getActualFields() != null ? fileInfo.getActualFields().size() : 0);
                                }
                            }
                        } else {
                            log.debug("无法识别文件类型: {}", filePath.getFileName());
                        }
                    } catch (Exception e) {
                        log.error("处理文件时出错: {}", filePath, e);
                    }
                });

        return fileInfoMap;
    }

    /**
     * 验证Excel文件并创建FileInfo对象 - 增强版
     * <p>
     * 这个方法是核心改进点：在原有验证基础上增加了数据解析和预览功能
     * 它采用了"渐进式增强"的设计理念，确保即使数据解析失败，基本验证仍然可用
     */
    private FileInfo validateAndCreateFileInfoWithPreview(Path filePath, TableTypeEnum tableType) {
        // 第一步：执行原有的基本验证逻辑
        FileInfo.FileInfoBuilder builder = FileInfo.builder()
                .fileName(filePath.getFileName().toString())
                .filePath(filePath.toAbsolutePath().toString())
                .tableType(tableType)
                .processingPriority(PROCESSING_PRIORITIES.get(tableType))
                .encoding("UTF-8"); // 默认编码

        try {
            // 获取基本文件信息
            long fileSize = Files.size(filePath);
            LocalDateTime lastModified = LocalDateTime.ofInstant(
                    Files.getLastModifiedTime(filePath).toInstant(),
                    java.time.ZoneId.systemDefault()
            );

            builder.fileSize(fileSize).lastModified(lastModified);

            // 第二步：使用EasyExcel进行深度数据解析
            ExcelParseResult parseResult = parseExcelWithEasyExcel(filePath, tableType);

            if (parseResult.isSuccess()) {
                // 数据解析成功，构建包含预览数据的FileInfo
                return builder
                        .isValid(true)
                        .sheetCount(1) // EasyExcel默认读取第一个sheet
                        .primarySheetName("Sheet1")
                        .estimatedRowCount(parseResult.getTotalRows())
                        .validRowCount(parseResult.getValidRows())
                        .actualFields(parseResult.getHeaders())
                        .previewData(parseResult.getPreviewData())
                        .dataQuality(parseResult.getDataQuality())
                        .qualityInfo(parseResult.getQualityInfo())
                        .build();
            } else {
                // 数据解析失败，但仍返回基本信息（向后兼容）
                return builder
                        .isValid(false)
                        .validationError("数据解析失败: " + parseResult.getErrorMessage())
                        .build();
            }

        } catch (IOException e) {
            return builder
                    .isValid(false)
                    .validationError("无法读取文件: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            // 即使出现意外错误，也要返回基本的文件信息
            log.warn("解析文件时出现异常，返回基本信息: {}", filePath, e);
            return builder
                    .isValid(false)
                    .validationError("文件解析异常: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 使用EasyExcel解析Excel文件 - 修复版
     * <p>
     * 关键修复：
     * 1. 正确设置表头行号为3（第三行才是真正的字段名）
     * 2. 增加了对前两行元数据的读取和验证
     * 3. 改进了数据质量评估逻辑
     */
    private ExcelParseResult parseExcelWithEasyExcel(Path filePath, TableTypeEnum tableType) {
        log.debug("开始使用EasyExcel解析文件: {}", filePath.getFileName());

        try {
            // 用于收集解析结果的容器
            ExcelDataCollector collector = new ExcelDataCollector();

            // 使用EasyExcel进行流式读取
            // 关键修复：设置headRowNumber为3，因为第3行才是真正的表头
            EasyExcel.read(filePath.toFile())
                    .sheet(0)
                    .headRowNumber(3)  // 修复：第3行是表头，而不是第1行
                    .registerReadListener(new AnalysisEventListener<Map<Integer, String>>() {

                        @Override
                        public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                            // 处理表头信息（现在正确读取第3行的字段名）
                            List<String> headers = headMap.values().stream()
                                    .filter(Objects::nonNull)
                                    .map(String::trim)
                                    .filter(header -> !header.isEmpty())
                                    .filter(header -> !"序号".equals(header)) // 过滤掉序号列
                                    .collect(Collectors.toList());

                            collector.setHeaders(headers);
                            log.debug("解析到表头: {}", headers);

                            // 验证表头是否包含必填字段
                            validateRequiredFields(tableType, headers, collector);
                        }

                        @Override
                        public void invoke(Map<Integer, String> data, AnalysisContext context) {
                            // 处理数据行（从第4行开始才是真正的数据）
                            collector.addDataRow(data);

                            // 限制预览数据的行数，避免内存问题
                            if (collector.getPreviewDataCount() < 5) {
                                Map<String, Object> previewRow = convertToPreviewFormat(
                                        collector.getHeaders(), data);
                                collector.addPreviewData(previewRow);
                            }
                        }

                        @Override
                        public void doAfterAllAnalysed(AnalysisContext context) {
                            log.debug("Excel解析完成，总行数: {}", collector.getTotalRows());
                        }
                    })
                    .doRead();

            // 额外读取前两行的元数据信息
            ExcelMetadata metadata = readExcelMetadata(filePath);
            collector.setMetadata(metadata);

            // 数据质量评估
            FileInfo.DataQualityInfo qualityInfo = assessDataQuality(
                    tableType, collector.getHeaders(), collector.getAllDataRows());

            // 构建解析结果
            return ExcelParseResult.builder()
                    .success(true)
                    .totalRows(collector.getTotalRows())
                    .validRows(collector.getValidRows())
                    .headers(collector.getHeaders())
                    .previewData(collector.getPreviewData())
                    .dataQuality(calculateDataQualityLevel(qualityInfo))
                    .qualityInfo(qualityInfo)
                    .metadata(metadata)
                    .build();

        } catch (Exception e) {
            log.error("EasyExcel解析失败: {}", filePath, e);
            return ExcelParseResult.builder()
                    .success(false)
                    .errorMessage("Excel解析失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 验证必填字段
     * 在表头解析完成后立即验证，确保所有必填字段都存在
     */
    private void validateRequiredFields(TableTypeEnum tableType, List<String> actualHeaders, ExcelDataCollector collector) {
        List<String> requiredFields = REQUIRED_FIELDS_CONFIG.getOrDefault(tableType, Collections.emptyList());

        List<String> missingFields = requiredFields.stream()
                .filter(field -> !actualHeaders.contains(field))
                .collect(Collectors.toList());

        if (!missingFields.isEmpty()) {
            collector.addValidationWarning("缺失必填字段: " + String.join(", ", missingFields));
            log.warn("表{}缺失必填字段: {}", tableType.getDescription(), missingFields);
        }

        log.info("字段验证完成 - 表类型: {}, 期望字段数: {}, 实际字段数: {}, 缺失字段数: {}",
                tableType.getDescription(), requiredFields.size(), actualHeaders.size(), missingFields.size());
    }

    /**
     * 读取Excel文件的元数据信息
     * 读取前两行的标题和说明信息
     */
    private ExcelMetadata readExcelMetadata(Path filePath) {
        try (Workbook workbook = WorkbookFactory.create(Files.newInputStream(filePath))) {
            Sheet sheet = workbook.getSheetAt(0);

            String title = "";
            String description = "";

            // 读取第一行作为标题
            if (sheet.getRow(0) != null && sheet.getRow(0).getCell(0) != null) {
                title = sheet.getRow(0).getCell(0).toString().trim();
            }

            // 读取第二行作为说明
            if (sheet.getRow(1) != null && sheet.getRow(1).getCell(0) != null) {
                description = sheet.getRow(1).getCell(0).toString().trim();
            }

            return ExcelMetadata.builder()
                    .title(title)
                    .description(description)
                    .headerRowIndex(2) // 第3行是表头（从0开始计数）
                    .dataStartRowIndex(3) // 第4行开始是数据
                    .build();

        } catch (Exception e) {
            log.warn("读取Excel元数据失败: {}", filePath, e);
            return ExcelMetadata.builder()
                    .title("未知")
                    .description("无法读取说明信息")
                    .headerRowIndex(2)
                    .dataStartRowIndex(3)
                    .build();
        }
    }

    /**
     * 转换为预览格式 - 改进版
     * 正确处理序号列和数据列的映射关系
     */
    private Map<String, Object> convertToPreviewFormat(List<String> headers, Map<Integer, String> data) {
        Map<String, Object> previewRow = new LinkedHashMap<>();

        // 跳过序号列（通常是第0列），从第1列开始映射到headers
        for (int i = 0; i < headers.size(); i++) {
            String fieldName = headers.get(i);
            // 数据列索引需要加1，因为第0列是序号列
            String fieldValue = data.get(i + 1);
            previewRow.put(fieldName, StringUtils.hasText(fieldValue) ? fieldValue.trim() : "");
        }

        return previewRow;
    }

    /**
     * 数据质量评估
     * 基于表类型和实际数据进行多维度质量评估
     */
    private FileInfo.DataQualityInfo assessDataQuality(TableTypeEnum tableType,
                                                       List<String> actualFields,
                                                       List<Map<Integer, String>> allData) {

        List<String> requiredFields = REQUIRED_FIELDS_CONFIG.getOrDefault(tableType, Collections.emptyList());

        // 计算缺失的必填字段
        List<String> missingRequired = requiredFields.stream()
                .filter(field -> !actualFields.contains(field))
                .collect(Collectors.toList());

        // 计算空值数量
        int nullValueCount = allData.stream()
                .mapToInt(row -> (int) row.values().stream()
                        .filter(value -> !StringUtils.hasText(value))
                        .count())
                .sum();

        // 计算重复行数量
        int duplicateCount = calculateDuplicateRows(allData);

        // 计算完整性评分
        int completenessScore = calculateCompletenessScore(requiredFields.size(), missingRequired.size());

        // 收集质量问题
        List<String> qualityIssues = new ArrayList<>();
        if (!missingRequired.isEmpty()) {
            qualityIssues.add("缺失必填字段: " + String.join(", ", missingRequired));
        }
        if (allData.size() > 0 && nullValueCount > allData.size() * actualFields.size() * 0.1) {
            qualityIssues.add(String.format("空值比例过高: %.1f%%",
                    (double) nullValueCount / (allData.size() * actualFields.size()) * 100));
        }
        if (duplicateCount > 0) {
            qualityIssues.add("存在重复数据: " + duplicateCount + "行");
        }

        return FileInfo.DataQualityInfo.builder()
                .missingRequiredFields(missingRequired.size())
                .nullValueCount(nullValueCount)
                .duplicateRowCount(duplicateCount)
                .completenessScore(completenessScore)
                .qualityIssues(qualityIssues)
                .build();
    }

    /**
     * 计算数据质量等级
     */
    private String calculateDataQualityLevel(FileInfo.DataQualityInfo qualityInfo) {
        int score = qualityInfo.getCompletenessScore();
        boolean hasIssues = !qualityInfo.getQualityIssues().isEmpty();

        if (score >= 90 && !hasIssues) {
            return "HIGH";
        } else if (score >= 70) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    /**
     * 计算重复行数量
     */
    private int calculateDuplicateRows(List<Map<Integer, String>> allData) {
        Set<String> uniqueRows = new HashSet<>();
        int duplicates = 0;

        for (Map<Integer, String> row : allData) {
            String rowString = row.values().stream()
                    .map(value -> value == null ? "" : value)
                    .collect(Collectors.joining("|"));

            if (!uniqueRows.add(rowString)) {
                duplicates++;
            }
        }

        return duplicates;
    }

    /**
     * 计算完整性评分
     */
    private int calculateCompletenessScore(int totalRequired, int missing) {
        if (totalRequired == 0) return 100;
        return Math.max(0, (totalRequired - missing) * 100 / totalRequired);
    }

    /**
     * 数据收集器内部类 - 增强版
     */
    private static class ExcelDataCollector {
        private List<String> headers = new ArrayList<>();
        private List<Map<Integer, String>> allDataRows = new ArrayList<>();
        private List<Map<String, Object>> previewData = new ArrayList<>();
        private List<String> validationWarnings = new ArrayList<>();
        private ExcelMetadata metadata;

        public void addDataRow(Map<Integer, String> row) {
            this.allDataRows.add(row);
        }

        public void addPreviewData(Map<String, Object> previewRow) {
            this.previewData.add(previewRow);
        }

        public void addValidationWarning(String warning) {
            this.validationWarnings.add(warning);
        }

        public int getTotalRows() {
            return allDataRows.size();
        }

        public int getValidRows() {
            return (int) allDataRows.stream()
                    .filter(this::isValidRow)
                    .count();
        }

        public int getPreviewDataCount() {
            return previewData.size();
        }

        private boolean isValidRow(Map<Integer, String> row) {
            // 判断是否为有效行：除了序号列外，至少有一个非空字段
            return row.entrySet().stream()
                    .filter(entry -> entry.getKey() > 0) // 跳过序号列
                    .anyMatch(entry -> StringUtils.hasText(entry.getValue()));
        }

        // Getters
        public List<String> getHeaders() {
            return headers;
        }

        public void setHeaders(List<String> headers) {
            this.headers = headers;
        }

        public List<Map<Integer, String>> getAllDataRows() {
            return allDataRows;
        }

        public List<Map<String, Object>> getPreviewData() {
            return previewData;
        }

        public List<String> getValidationWarnings() {
            return validationWarnings;
        }

        public ExcelMetadata getMetadata() {
            return metadata;
        }

        public void setMetadata(ExcelMetadata metadata) {
            this.metadata = metadata;
        }
    }

    /**
     * Excel元数据信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ExcelMetadata {
        private String title;           // 第一行的标题
        private String description;     // 第二行的说明
        private int headerRowIndex;     // 表头行索引
        private int dataStartRowIndex;  // 数据开始行索引
    }

    /**
     * Excel解析结果内部类 - 增强版
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ExcelParseResult {
        private boolean success;
        private String errorMessage;
        private int totalRows;
        private int validRows;
        private List<String> headers;
        private List<Map<String, Object>> previewData;
        private String dataQuality;
        private FileInfo.DataQualityInfo qualityInfo;
        private ExcelMetadata metadata;  // 新增：元数据信息
    }
}