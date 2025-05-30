package cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 医疗机构资源信息 Response VO")
@Data
@ExcelIgnoreUnannotated
@Accessors(chain = false)
public class HosResourceInfoRespVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "17308")
    private Long id;

    @Schema(description = "系统编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String domainCode;

    @Schema(description = "数据上报日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "数据上报日期")
    private String uploadDate;

    @Schema(description = "省级行政区划代码")
    @ExcelProperty(value = "省级行政区划代码")
    private String provinceCode;

    @Schema(description = "组织机构代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "组织机构代码")
    private String organizationCode;

    @Schema(description = "医疗机构代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("医疗机构代码")
    private String hospitalCode;

    @Schema(description = "组织机构名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @ExcelProperty("组织机构名称")
    private String organizationName;

    @Schema(description = "年度药品总收入（元）")
    @ExcelProperty(value = "年度药品总收入（元）")
    private BigDecimal annualDrugIncome;

    @Schema(description = "实有床位数")
    @ExcelProperty(value = "实有床位数")
    private Integer bedsNum;

    @Schema(description = "统计日期(yyyyMMdd)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String statDate;

    @Schema(description = "执业医师数")
//    @ExcelProperty(value = "执业医师数", converter = ExcelDataConverter.SafeLongConverter.class)
    private Integer pracDockerNum;

    @Schema(description = "执业助理医师数")
//    @ExcelProperty(value = "执业助理医师数", converter = ExcelDataConverter.SafeLongConverter.class)
    private Integer assDockerNum;

    @Schema(description = "总诊疗人次数", example = "13675")
//    @ExcelProperty(value = "总诊疗人次数", converter = ExcelDataConverter.SafeLongConverter.class)
    private Integer visitCount;

    @Schema(description = "出院人数", example = "9451")
//    @ExcelProperty(value = "出院人数", converter = ExcelDataConverter.SafeLongConverter.class)
    private Integer leaveHosCount;

    @Schema(description = "本季度药品总收入(元)")
//    @ExcelProperty(value = "本季度药品总收入(元)", converter = ExcelDataConverter.SafeBigDecimalConverter.class)
    private BigDecimal drugSellAmount;

    @Schema(description = "本季度中药饮片总采购额")
//    @ExcelProperty(value = "本季度中药饮片总采购额", converter = ExcelDataConverter.SafeBigDecimalConverter.class)
    private BigDecimal ypPurchaseAmount;

    @Schema(description = "年度药品总收入（元）")
//    @ExcelProperty(value = "本季度中药饮片总销售额", converter = ExcelDataConverter.SafeBigDecimalConverter.class)
    private BigDecimal ypSellAmount;

    @Schema(description = "本季度中药颗粒剂总采购额")
//    @ExcelProperty(value = "本季度中药颗粒剂总采购额", converter = ExcelDataConverter.SafeBigDecimalConverter.class)
    private BigDecimal klPurchaseAmount;

    @Schema(description = "本季度中药颗粒剂总销售额")
//    @ExcelProperty(value = "本季度中药颗粒剂总销售额", converter = ExcelDataConverter.SafeBigDecimalConverter.class)
    private BigDecimal klSellAmount;

    @Schema(description = "导入批次号")
    private String importBatchNo;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}