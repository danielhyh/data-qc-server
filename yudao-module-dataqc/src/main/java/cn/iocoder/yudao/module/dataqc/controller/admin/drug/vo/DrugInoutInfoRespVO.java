package cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - 药品出入库 Response VO")
@Data
@ExcelIgnoreUnannotated
public class DrugInoutInfoRespVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10445")
    @ExcelProperty("主键ID")
    private Long id;

    @Schema(description = "流水号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("流水号")
    private Long serialNum;

    @Schema(description = "系统编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("系统编码")
    private String domainCode;

    @Schema(description = "组织机构代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("组织机构代码")
    private String organizationCode;

    @Schema(description = "组织机构名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @ExcelProperty("组织机构名称")
    private String organizationName;

    @Schema(description = "医疗机构代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("医疗机构代码")
    private String hospitalCode;

    @Schema(description = "数据上报日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("数据上报日期")
    private String uploadDate;

    @Schema(description = "国家药管平台药品编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "27389")
    @ExcelProperty("国家药管平台药品编码")
    private String ypid;

    @Schema(description = "省级药品集中采购平台药品编码", example = "5070")
    @ExcelProperty("省级药品集中采购平台药品编码")
    private String prDrugId;

    @Schema(description = "院内药品唯一码", requiredMode = Schema.RequiredMode.REQUIRED, example = "12699")
    @ExcelProperty("院内药品唯一码")
    private String hosDrugId;

    @Schema(description = "产品通用名", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @ExcelProperty("产品通用名")
    private String productName;

    @Schema(description = "出入库时间(yyyyMMdd)", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("出入库时间(yyyyMMdd)")
    private String outInDate;

    @Schema(description = "出入库类型(IN-入库,OUT-出库)", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("出入库类型(IN-入库,OUT-出库)")
    private String ioType;

    @Schema(description = "入库数量(最小销售包装单位)")
    @ExcelProperty("入库数量(最小销售包装单位)")
    private Long inPackQuantity;

    @Schema(description = "入库数量(最小制剂单位)")
    @ExcelProperty("入库数量(最小制剂单位)")
    private Long inDosageQuantity;

    @Schema(description = "入库价格(最小销售包装单位)", example = "31445")
    @ExcelProperty("入库价格(最小销售包装单位)")
    private BigDecimal inPackPrice;

    @Schema(description = "入库价格(最小制剂单位)", example = "26800")
    @ExcelProperty("入库价格(最小制剂单位)")
    private BigDecimal inDosagePrice;

    @Schema(description = "出库数量(最小销售包装单位)")
    @ExcelProperty("出库数量(最小销售包装单位)")
    private Long outPackQuantity;

    @Schema(description = "出库数量(最小制剂单位)")
    @ExcelProperty("出库数量(最小制剂单位)")
    private Long outDosageQuantity;

    @Schema(description = "供应商代码")
    @ExcelProperty("供应商代码")
    private String supplierCode;

    @Schema(description = "供应商名称", example = "赵六")
    @ExcelProperty("供应商名称")
    private String supplierName;

    @Schema(description = "批号")
    @ExcelProperty("批号")
    private String batchNo;

    @Schema(description = "生产日期")
    @ExcelProperty("生产日期")
    private String productionDate;

    @Schema(description = "有效期至")
    @ExcelProperty("有效期至")
    private String expiryDate;

    @Schema(description = "导入批次号")
    @ExcelProperty("导入批次号")
    private String importBatchNo;

    @Schema(description = "导入时间")
    @ExcelProperty("导入时间")
    private LocalDateTime importTime;

    @Schema(description = "数据来源(IN_IMPORT-入库导入,OUT_IMPORT-出库导入)", example = "2")
    @ExcelProperty("数据来源(IN_IMPORT-入库导入,OUT_IMPORT-出库导入)")
    private String sourceType;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}