package cn.iocoder.yudao.module.drug.service.batch;

import cn.iocoder.yudao.module.drug.enums.TableTypeEnum;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 药品导入模板服务
 * <p>
 * 设计理念：
 * 1. 模板标准化：提供统一的Excel模板格式，确保数据导入的一致性
 * 2. 动态生成：根据业务规则动态生成模板，支持不同场景的需求
 * 3. 版本管理：支持模板版本控制，便于后续维护和升级
 * 4. 示例数据：提供标准的示例数据，帮助用户理解填写规范
 */
@Service
@Slf4j
public class DrugTemplateService {

    // 模板文件存储路径
    private static final String TEMPLATE_BASE_PATH = "/templates/drug-import/";

    // 各表模板配置
    private static final Map<TableTypeEnum, TemplateConfig> TEMPLATE_CONFIGS = Map.of(
            TableTypeEnum.HOSPITAL_INFO, new TemplateConfig("机构基本情况.xlsx", "机构信息", List.of(
                    "机构编码", "机构名称", "机构等级", "所在地区", "联系电话", "负责人", "床位数", "医生数", "药师数", "建院时间"
            )),
            TableTypeEnum.DRUG_CATALOG, new TemplateConfig("药品目录.xlsx", "药品目录", List.of(
                    "药品编码", "药品名称", "通用名", "规格", "剂型", "生产厂家", "批准文号", "价格", "分类", "是否基药"
            )),
            TableTypeEnum.DRUG_INBOUND, new TemplateConfig("药品入库.xlsx", "入库情况", List.of(
                    "入库单号", "药品编码", "药品名称", "规格", "入库数量", "入库单价", "入库金额", "供应商", "入库日期", "批次号"
            )),
            TableTypeEnum.DRUG_OUTBOUND, new TemplateConfig("药品出库.xlsx", "出库情况", List.of(
                    "出库单号", "药品编码", "药品名称", "规格", "出库数量", "出库单价", "出库金额", "领用科室", "出库日期", "用途"
            )),
            TableTypeEnum.DRUG_USAGE, new TemplateConfig("药品使用.xlsx", "使用情况", List.of(
                    "处方号", "药品编码", "药品名称", "规格", "使用数量", "使用单价", "使用金额", "患者信息", "开药医生", "使用日期"
            ))
    );

    /**
     * 下载导入模板
     * <p>
     * 根据模板类型生成对应的Excel模板文件，并打包成ZIP文件供下载
     */
    public void downloadTemplate(String templateType, HttpServletResponse response) throws IOException {
        log.info("开始生成导入模板: templateType={}", templateType);

        // 设置响应头
        String fileName = String.format("药品数据导入模板_%s.zip", templateType);
        response.setContentType("application/zip");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

        try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {

            if ("STANDARD".equals(templateType)) {
                // 生成标准模板包（包含所有表的模板）
                generateStandardTemplatePackage(zipOut);
            } else {
                // 生成特定表的模板
                TableTypeEnum tableType = TableTypeEnum.valueOf(templateType);
                generateSingleTemplate(zipOut, tableType);
            }

            // 添加说明文档
            addInstructionDocument(zipOut);

            zipOut.flush();
        }

        log.info("模板生成完成: templateType={}", templateType);
    }

    /**
     * 生成标准模板包
     * 包含所有业务表的Excel模板文件
     */
    private void generateStandardTemplatePackage(ZipOutputStream zipOut) throws IOException {
        for (TableTypeEnum tableType : TableTypeEnum.values()) {
            generateSingleTemplate(zipOut, tableType);
        }
    }

    /**
     * 生成单个表的Excel模板
     */
    private void generateSingleTemplate(ZipOutputStream zipOut, TableTypeEnum tableType) throws IOException {
        TemplateConfig config = TEMPLATE_CONFIGS.get(tableType);

        // 创建ZIP条目
        ZipEntry zipEntry = new ZipEntry(config.getFileName());
        zipOut.putNextEntry(zipEntry);

        // 创建Excel工作簿
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(config.getSheetName());

            // 创建标题样式
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle exampleStyle = createExampleStyle(workbook);

            // 创建标题行
            Row titleRow = sheet.createRow(0);
            for (int i = 0; i < config.getColumns().size(); i++) {
                Cell cell = titleRow.createCell(i);
                cell.setCellValue(config.getColumns().get(i));
                cell.setCellStyle(titleStyle);

                // 设置列宽
                sheet.setColumnWidth(i, 4000);
            }

            // 添加示例数据行
            addExampleData(sheet, tableType, config, exampleStyle);

            // 添加数据验证
            addDataValidation(sheet, tableType, config);

            // 写入ZIP流
            workbook.write(zipOut);
        }

        zipOut.closeEntry();
    }

    /**
     * 创建标题样式
     */
    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // 设置背景色
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 设置字体
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);

        // 设置边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // 设置对齐
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    /**
     * 创建示例数据样式
     */
    private CellStyle createExampleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // 设置背景色
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 设置字体
        Font font = workbook.createFont();
        font.setItalic(true);
        font.setColor(IndexedColors.DARK_GREEN.getIndex());
        style.setFont(font);

        // 设置边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    /**
     * 添加示例数据
     */
    private void addExampleData(Sheet sheet, TableTypeEnum tableType,
                                TemplateConfig config, CellStyle exampleStyle) {
        Map<TableTypeEnum, String[]> exampleData = getExampleData();
        String[] examples = exampleData.get(tableType);

        if (examples != null) {
            Row exampleRow = sheet.createRow(1);
            for (int i = 0; i < examples.length && i < config.getColumns().size(); i++) {
                Cell cell = exampleRow.createCell(i);
                cell.setCellValue(examples[i]);
                cell.setCellStyle(exampleStyle);
            }
        }
    }

    /**
     * 获取示例数据
     */
    private Map<TableTypeEnum, String[]> getExampleData() {
        return Map.of(
                TableTypeEnum.HOSPITAL_INFO, new String[]{
                        "H001", "某某市人民医院", "三级甲等", "某某市", "0123-12345678", "张主任", "500", "120", "30", "1980-01-01"
                },
                TableTypeEnum.DRUG_CATALOG, new String[]{
                        "D001", "阿莫西林胶囊", "阿莫西林", "0.25g*24粒", "胶囊剂", "某某制药", "国药准字H12345678", "15.50", "抗感染药", "是"
                },
                TableTypeEnum.DRUG_INBOUND, new String[]{
                        "RK20241201001", "D001", "阿莫西林胶囊", "0.25g*24粒", "100", "15.50", "1550.00", "某某医药公司", "2024-12-01", "20241201"
                },
                TableTypeEnum.DRUG_OUTBOUND, new String[]{
                        "CK20241201001", "D001", "阿莫西林胶囊", "0.25g*24粒", "50", "15.50", "775.00", "内科", "2024-12-01", "临床使用"
                },
                TableTypeEnum.DRUG_USAGE, new String[]{
                        "CF20241201001", "D001", "阿莫西林胶囊", "0.25g*24粒", "1", "15.50", "15.50", "张三/男/35岁", "李医生", "2024-12-01"
                }
        );
    }

    /**
     * 添加数据验证
     */
    private void addDataValidation(Sheet sheet, TableTypeEnum tableType, TemplateConfig config) {
        DataValidationHelper validationHelper = sheet.getDataValidationHelper();

        // 为特定列添加数据验证
        switch (tableType) {
            case HOSPITAL_INFO:
                // 机构等级验证
                addDropdownValidation(sheet, validationHelper, 0, 2,
                        new String[]{"三级甲等", "三级乙等", "二级甲等", "二级乙等", "一级"});
                break;

            case DRUG_CATALOG:
                // 是否基药验证
                addDropdownValidation(sheet, validationHelper, 0, 9,
                        new String[]{"是", "否"});
                break;

            default:
                // 其他表暂时不添加特殊验证
                break;
        }
    }

    /**
     * 添加下拉列表验证
     */
    private void addDropdownValidation(Sheet sheet, DataValidationHelper validationHelper,
                                       int firstRow, int colIndex, String[] options) {
        try {
            DataValidationConstraint constraint = validationHelper.createExplicitListConstraint(options);
            CellRangeAddressList addressList = new CellRangeAddressList(
                    firstRow + 1, 1000, colIndex, colIndex); // 从第二行开始，到第1000行
            DataValidation validation = validationHelper.createValidation(constraint, addressList);
            validation.setSuppressDropDownArrow(false);
            validation.setShowErrorBox(true);
            sheet.addValidationData(validation);
        } catch (Exception e) {
            log.warn("添加数据验证失败: colIndex={}", colIndex, e);
        }
    }

    /**
     * 添加说明文档
     */
    private void addInstructionDocument(ZipOutputStream zipOut) throws IOException {
        ZipEntry instructionEntry = new ZipEntry("导入说明.txt");
        zipOut.putNextEntry(instructionEntry);

        String instruction = buildInstructionText();
        zipOut.write(instruction.getBytes(StandardCharsets.UTF_8));
        zipOut.closeEntry();
    }

    /**
     * 构建说明文档内容
     */
    private String buildInstructionText() {
        return """
                药品数据导入模板使用说明
                            
                1. 模板结构说明：
                   - 机构基本情况.xlsx：医疗机构的基础信息
                   - 药品目录.xlsx：医疗机构的药品目录清单
                   - 药品入库.xlsx：药品采购入库记录
                   - 药品出库.xlsx：药品发放出库记录
                   - 药品使用.xlsx：药品临床使用明细
                            
                2. 填写规范：
                   - 第一行为列标题，请勿修改
                   - 第二行为示例数据，可以删除或替换为实际数据
                   - 所有必填字段不能为空
                   - 日期格式统一使用：YYYY-MM-DD
                   - 数值字段请使用数字格式，不要包含文字
                            
                3. 数据关联关系：
                   - 机构编码需要在所有表中保持一致
                   - 药品编码需要先在药品目录中存在
                   - 建议按顺序导入：机构信息 → 药品目录 → 其他业务数据
                            
                4. 质量控制：
                   - 系统会自动进行数据格式验证
                   - 会检查数据间的关联关系
                   - 不符合规范的数据会在质控环节被标记
                            
                5. 技术支持：
                   - 如遇到问题，请联系系统管理员
                   - 或查看系统帮助文档获取更多信息
                            
                版本：v1.0
                更新时间：2024-12-01
                """;
    }

    /**
     * 模板配置内部类
     */
    private static class TemplateConfig {
        private final String fileName;
        private final String sheetName;
        private final List<String> columns;

        public TemplateConfig(String fileName, String sheetName, List<String> columns) {
            this.fileName = fileName;
            this.sheetName = sheetName;
            this.columns = columns;
        }

        public String getFileName() {
            return fileName;
        }

        public String getSheetName() {
            return sheetName;
        }

        public List<String> getColumns() {
            return columns;
        }
    }
}