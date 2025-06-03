package cn.iocoder.yudao.module.drug.service.batch;

import cn.iocoder.yudao.module.drug.controller.admin.batch.vo.FileExtractResult;
import cn.iocoder.yudao.module.drug.controller.admin.batch.vo.FileInfo;
import cn.iocoder.yudao.module.drug.enums.TableTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
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
 * 
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
        TableTypeEnum.HOSPITAL_INFO, Pattern.compile(".*机构.*信息.*\\.xlsx?$", Pattern.CASE_INSENSITIVE),
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
     * 解压并验证上传的文件
     * <p>
     * 这是整个服务的入口方法，它协调了完整的文件处理流程。
     * 就像一个项目经理，负责统筹各个工作环节，确保整体目标的达成。
     *
     * @param taskId 任务ID，用于创建独立的工作目录
     * @param file   上传的压缩文件
     * @return 解压和验证的结果
     */
    public FileExtractResult extractAndValidate(Long taskId, MultipartFile file) {
        LocalDateTime startTime = LocalDateTime.now();
        log.info("开始解压和验证文件: taskId={}, fileName={}, fileSize={}",
                taskId, file.getOriginalFilename(), file.getSize());

        try {
            // 第一步：基础验证
            // 这一步就像门卫检查访客证件，确保来访者符合基本要求
            validateUploadedFile(file);

            // 第二步：创建工作目录
            // 为每个任务创建独立的工作空间，避免文件冲突
            Path workDir = createWorkDirectory(taskId);

            // 第三步：保存上传文件
            // 将内存中的文件持久化到磁盘，为后续处理做准备
            Path savedFilePath = saveUploadedFile(file, workDir);

            // 第四步：解压文件
            // 这是核心处理环节，需要小心处理各种异常情况
            Path extractDir = extractArchiveFile(savedFilePath, workDir);

            // 第五步：扫描和验证Excel文件
            // 找出所有有用的Excel文件，并建立与业务表的映射关系
            Map<TableTypeEnum, FileInfo> fileInfoMap = scanAndValidateExcelFiles(extractDir);

            // 第六步：构建结果对象
            LocalDateTime endTime = LocalDateTime.now();
            long durationMs = java.time.Duration.between(startTime, endTime).toMillis();

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
            log.error("文件解压和验证失败: taskId={}, fileName={}",
                    taskId, file.getOriginalFilename(), e);

            // 构建失败结果，确保调用方能够获得明确的错误信息
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
     * 验证上传的文件
     * <p>
     * 这个方法实现了"快速失败"原则，在处理的最早阶段发现并报告问题。
     * 就像质检员在生产线的入口把关，避免不合格原料进入后续流程。
     */
    private void validateUploadedFile(MultipartFile file) {
        // 检查文件是否为空
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        // 检查文件名是否存在
        String originalFilename = file.getOriginalFilename();
        if (originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        // 检查文件扩展名
        String fileExtension = getFileExtension(originalFilename).toLowerCase();
        if (!SUPPORTED_ARCHIVE_EXTENSIONS.contains(fileExtension)) {
            throw new IllegalArgumentException("不支持的文件格式，仅支持 ZIP 和 RAR 格式");
        }

        // 检查文件大小（限制为100MB）
        long maxSize = 100 * 1024 * 1024L; // 100MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小超过限制，最大支持 100MB");
        }

        log.debug("文件验证通过: fileName={}, size={}KB", 
                originalFilename, file.getSize() / 1024);
    }

    /**
     * 创建任务工作目录
     * <p>
     * 为每个任务创建独立的工作空间，这种隔离策略有多个好处：
     * 1. 避免不同任务之间的文件冲突
     * 2. 便于任务完成后的清理工作
     * 3. 提供清晰的文件组织结构
     */
    private Path createWorkDirectory(Long taskId) throws IOException {
        String baseDir = System.getProperty("java.io.tmpdir", "/tmp");
        Path workDir = Paths.get(baseDir, "drug-import", "task-" + taskId);
        
        // 如果目录已存在，先清理再创建（支持任务重试）
        if (Files.exists(workDir)) {
            deleteDirectoryRecursively(workDir);
        }
        
        Files.createDirectories(workDir);
        log.debug("创建工作目录: {}", workDir.toAbsolutePath());
        
        return workDir;
    }

    /**
     * 保存上传的文件到工作目录
     */
    private Path saveUploadedFile(MultipartFile file, Path workDir) throws IOException {
        String originalFilename = file.getOriginalFilename();
        Path targetPath = workDir.resolve("uploaded_" + originalFilename);
        
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
        
        log.debug("文件保存成功: {}", targetPath.toAbsolutePath());
        return targetPath;
    }

    /**
     * 解压归档文件
     * <p>
     * 这个方法处理不同类型的压缩文件，目前主要支持ZIP格式。
     * 设计时考虑了扩展性，未来可以轻松添加对RAR等格式的支持。
     */
    private Path extractArchiveFile(Path archiveFile, Path workDir) throws IOException {
        Path extractDir = workDir.resolve("extracted");
        Files.createDirectories(extractDir);

        String fileExtension = getFileExtension(archiveFile.getFileName().toString());
        
        if (".zip".equalsIgnoreCase(fileExtension)) {
            extractZipFile(archiveFile, extractDir);
        } else if (".rar".equalsIgnoreCase(fileExtension)) {
            // RAR文件解压需要第三方库，这里先抛出异常提示
            throw new UnsupportedOperationException("暂不支持RAR格式，请使用ZIP格式");
        } else {
            throw new IllegalArgumentException("不支持的压缩格式: " + fileExtension);
        }

        log.debug("文件解压完成: {} -> {}", archiveFile, extractDir);
        return extractDir;
    }

    /**
     * 解压ZIP文件
     * <p>
     * 这个方法实现了安全的ZIP文件解压，包含了对Zip Bomb攻击的防护措施。
     * 设置了合理的限制来防止恶意文件消耗过多系统资源。
     */
    private void extractZipFile(Path zipFile, Path extractDir) throws IOException {
        final long MAX_TOTAL_SIZE = 500 * 1024 * 1024L; // 500MB解压后总大小限制
        final int MAX_FILE_COUNT = 100; // 最大文件数量限制
        
        long totalSize = 0;
        int fileCount = 0;

        try (ZipInputStream zipIn = new ZipInputStream(
                new BufferedInputStream(Files.newInputStream(zipFile)))) {
            
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                fileCount++;
                
                // 防止Zip Bomb攻击
                if (fileCount > MAX_FILE_COUNT) {
                    throw new IOException("压缩文件包含过多文件，最多支持 " + MAX_FILE_COUNT + " 个文件");
                }

                // 跳过目录条目
                if (entry.isDirectory()) {
                    continue;
                }

                // 验证文件路径，防止目录遍历攻击
                String entryName = entry.getName();
                if (entryName.contains("..") || entryName.startsWith("/")) {
                    log.warn("跳过可疑路径的文件: {}", entryName);
                    continue;
                }

                // 创建目标文件路径
                Path targetFile = extractDir.resolve(entryName);
                Files.createDirectories(targetFile.getParent());

                // 解压文件内容
                try (OutputStream out = Files.newOutputStream(targetFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    
                    while ((bytesRead = zipIn.read(buffer)) != -1) {
                        totalSize += bytesRead;
                        
                        // 检查总大小限制
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
     * 扫描并验证Excel文件
     * <p>
     * 这是整个服务的核心逻辑，负责从解压的文件中识别出有用的Excel文件，
     * 并建立与业务表类型的映射关系。这个过程就像图书管理员给新到的图书分类编目。
     */
    private Map<TableTypeEnum, FileInfo> scanAndValidateExcelFiles(Path extractDir) throws IOException {
        Map<TableTypeEnum, FileInfo> fileInfoMap = new HashMap<>();
        
        // 遍历解压目录中的所有文件
        Files.walk(extractDir)
                .filter(Files::isRegularFile)
                .filter(this::isExcelFile)
                .forEach(filePath -> {
                    try {
                        // 尝试识别文件对应的表类型
                        TableTypeEnum tableType = identifyTableType(filePath.getFileName().toString());
                        
                        if (tableType != null) {
                            // 验证Excel文件格式
                            FileInfo fileInfo = validateAndCreateFileInfo(filePath, tableType);
                            
                            if (fileInfo.getIsValid()) {
                                // 检查是否有重复的表类型文件
                                if (fileInfoMap.containsKey(tableType)) {
                                    log.warn("发现重复的表类型文件: {} 和 {}", 
                                            fileInfoMap.get(tableType).getFileName(), 
                                            fileInfo.getFileName());
                                } else {
                                    fileInfoMap.put(tableType, fileInfo);
                                    log.info("识别到有效文件: {} -> {}", 
                                            fileInfo.getFileName(), tableType.getDescription());
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
     * 验证Excel文件并创建FileInfo对象
     * <p>
     * 这个方法深入Excel文件内部，检查其结构和内容是否符合预期。
     * 就像医生给病人做全面体检，确保各项指标都正常。
     */
    private FileInfo validateAndCreateFileInfo(Path filePath, TableTypeEnum tableType) {
        FileInfo.FileInfoBuilder builder = FileInfo.builder()
                .fileName(filePath.getFileName().toString())
                .filePath(filePath.toAbsolutePath().toString())
                .tableType(tableType)
                .processingPriority(PROCESSING_PRIORITIES.get(tableType));

        try {
            // 获取文件基本信息
            long fileSize = Files.size(filePath);
            LocalDateTime lastModified = LocalDateTime.ofInstant(
                    Files.getLastModifiedTime(filePath).toInstant(),
                    java.time.ZoneId.systemDefault()
            );

            builder.fileSize(fileSize).lastModified(lastModified);

            // 验证Excel文件结构
            try (Workbook workbook = WorkbookFactory.create(Files.newInputStream(filePath))) {
                int sheetCount = workbook.getNumberOfSheets();
                
                if (sheetCount == 0) {
                    return builder
                            .isValid(false)
                            .validationError("Excel文件不包含任何工作表")
                            .build();
                }

                Sheet firstSheet = workbook.getSheetAt(0);
                String primarySheetName = firstSheet.getSheetName();
                
                // 估算数据行数（跳过标题行）
                int estimatedRowCount = Math.max(0, firstSheet.getLastRowNum());

                return builder
                        .isValid(true)
                        .sheetCount(sheetCount)
                        .primarySheetName(primarySheetName)
                        .estimatedRowCount(estimatedRowCount)
                        .build();
                        
            } catch (Exception e) {
                return builder
                        .isValid(false)
                        .validationError("Excel文件格式错误: " + e.getMessage())
                        .build();
            }

        } catch (IOException e) {
            return builder
                    .isValid(false)
                    .validationError("无法读取文件: " + e.getMessage())
                    .build();
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
     * 递归删除目录
     * <p>
     * 这个工具方法用于清理工作目录，确保不会留下垃圾文件。
     */
    private void deleteDirectoryRecursively(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return;
        }

        Files.walk(directory)
                .sorted(Comparator.reverseOrder()) // 先删除文件，后删除目录
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        log.warn("删除文件失败: {}", path, e);
                    }
                });
    }
}