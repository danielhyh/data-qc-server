package cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo;

import cn.iocoder.yudao.module.dataqc.util.ExcelDataConverter;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Schema(description = "药品入库数据导入 VO")
@Data
@ExcelIgnoreUnannotated
@Accessors(chain = false)
public class DrugInInfoRespVO {
    
    // 共同字段
    @Schema(description = "数据上报日期")
    @ExcelProperty("数据上报日期")
    private String uploadDate;

    @Schema(description = "省级行政区划代码") //+
    @ExcelProperty(value = "省级行政区划代码")
    private String provinceCode;

    @Schema(description = "组织机构代码")
    @ExcelProperty("组织机构代码")
    private String organizationCode;

    @Schema(description = "医疗机构代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("医疗机构代码")
    private String hospitalCode;

    @Schema(description = "组织机构名称")
    @ExcelProperty("组织机构名称")
    private String organizationName;

    @Schema(description = "国家药管平台药品编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "27389")
    @ExcelProperty("国家药品编码（YPID）")
    private String ypid;

    @Schema(description = "院内药品唯一码")
    @ExcelProperty("院内药品唯一码")
    private String hosDrugId;

    @Schema(description = "省级药品集中采购平台药品编码", example = "5070")
    @ExcelProperty("省级药品集中采购平台药品编码")
    private String prDrugId;

    @Schema(description = "产品通用名")
    @ExcelProperty("产品名称")
    private String productName;

    // 入库专用字段
    @Schema(description = "入库总金额(元)")
    @ExcelProperty(value = "入库总金额（元）", converter = ExcelDataConverter.SafeBigDecimalConverter.class)
    private BigDecimal inTotalPrice;

    @Schema(description = "入库数量(最小销售包装单位)")
    @ExcelProperty(value = "入库数量（最小销售包装单位）", converter = ExcelDataConverter.SafeLongConverter.class)
    private Long inPackQuantity;

    @Schema(description = "入库数量(最小制剂单位)")
    @ExcelProperty(value = "入库数量（最小制剂单位）", converter = ExcelDataConverter.SafeLongConverter.class)
    private Long inDosageQuantity;
}