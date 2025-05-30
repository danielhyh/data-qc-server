package cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo;

import cn.iocoder.yudao.module.dataqc.util.ExcelDataConverter;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 药品出入库 Response VO")
@Data
@ExcelIgnoreUnannotated
@Accessors(chain = true)
public class DrugInoutInfoRespVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10445")
    private Long id;

    @Schema(description = "数据上报日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("数据上报日期")
    private String uploadDate;

    @Schema(description = "省级行政区划代码") //+
    @ExcelProperty(value = "省级行政区划代码")
    private String provinceCode;

    @Schema(description = "组织机构代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("组织机构代码")
    private String organizationCode;

    @Schema(description = "组织机构名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @ExcelProperty("组织机构名称")
    private String organizationName;

    @Schema(description = "医疗机构代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("医疗机构代码")
    private String hospitalCode;

    @Schema(description = "国家药管平台药品编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "27389")
    @ExcelProperty("国家药品编码（YPID）")
    private String ypid;

    @Schema(description = "院内药品唯一码", requiredMode = Schema.RequiredMode.REQUIRED, example = "12699")
    @ExcelProperty("院内药品唯一码")
    private String hosDrugId;

    @Schema(description = "省级药品集中采购平台药品编码", example = "5070")
    @ExcelProperty("省级药品集中采购平台药品编码")
    private String prDrugId;

    @Schema(description = "产品通用名", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @ExcelProperty("产品名称")
    private String productName;

    @Schema(description = "出库数量(最小销售包装单位)")
    @ExcelProperty(value = "出库数量（最小销售包装单位）", converter = ExcelDataConverter.SafeLongConverter.class)
    private Long outPackQuantity;

    @Schema(description = "出库数量(最小制剂单位)")
    @ExcelProperty(value = "出库数量（最小制剂单位）", converter = ExcelDataConverter.SafeLongConverter.class)
    private Long outDosageQuantity;

    // 入库总金额(元) +
    @Schema(description = "入库总金额(元)", requiredMode = Schema.RequiredMode.REQUIRED, example = "26800")
    @ExcelProperty(value = "入库总金额(元)", converter = ExcelDataConverter.SafeBigDecimalConverter.class)
    private BigDecimal inTotalPrice;

    @Schema(description = "入库数量(最小销售包装单位)")
    @ExcelProperty(value = "入库数量（最小销售包装单位）", converter = ExcelDataConverter.SafeLongConverter.class)
    private Long inPackQuantity;

    @Schema(description = "入库数量(最小制剂单位)")
    @ExcelProperty(value = "入库数量（最小制剂单位）", converter = ExcelDataConverter.SafeLongConverter.class)
    private Long inDosageQuantity;

    @Schema(description = "流水号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long serialNum;

    @Schema(description = "系统编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String domainCode;

    @Schema(description = "出入库时间(yyyyMMdd)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String outInDate;

    @Schema(description = "出入库类型(IN-入库,OUT-出库)", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private String ioType;

    @Schema(description = "入库价格(最小制剂单位)", example = "26800")
    private BigDecimal inDosagePrice;

    @Schema(description = "供应商代码")
    private String supplierCode;

    @Schema(description = "供应商名称", example = "赵六")
    private String supplierName;

    @Schema(description = "批号")
    private String batchNo;

    @Schema(description = "生产日期")
    private String productionDate;

    @Schema(description = "有效期至")
    private String expiryDate;

    @Schema(description = "导入批次号")
    private String importBatchNo;

    @Schema(description = "导入时间")
    private LocalDateTime importTime;

    @Schema(description = "数据来源(IN_IMPORT-入库导入,OUT_IMPORT-出库导入)", example = "2")
    private String sourceType;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}