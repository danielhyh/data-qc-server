package cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 医疗机构资源信息 Response VO")
@Data
@ExcelIgnoreUnannotated
public class HosResourceInfoRespVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "17308")
    @ExcelProperty("主键ID")
    private Long id;

    @Schema(description = "系统编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("系统编码")
    private String domainCode;

    @Schema(description = "组织机构代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("组织机构代码")
    private String organizationCode;

    @Schema(description = "组织机构名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @ExcelProperty("组织机构名称")
    private String organizationName;

    @Schema(description = "医疗机构代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("医疗机构代码")
    private String hospitalCode;

    @Schema(description = "统计日期(yyyyMMdd)", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("统计日期(yyyyMMdd)")
    private String statDate;

    @Schema(description = "实有床位数")
    @ExcelProperty("实有床位数")
    private Integer bedsNum;

    @Schema(description = "执业医师数")
    @ExcelProperty("执业医师数")
    private Integer pracDockerNum;

    @Schema(description = "执业助理医师数")
    @ExcelProperty("执业助理医师数")
    private Integer assDockerNum;

    @Schema(description = "总诊疗人次数", example = "13675")
    @ExcelProperty("总诊疗人次数")
    private Integer visitCount;

    @Schema(description = "出院人数", example = "9451")
    @ExcelProperty("出院人数")
    private Integer leaveHosCount;

    @Schema(description = "本季度药品总收入(元)")
    @ExcelProperty("本季度药品总收入(元)")
    private BigDecimal drugSellAmount;

    @Schema(description = "本季度中药饮片总采购额")
    @ExcelProperty("本季度中药饮片总采购额")
    private BigDecimal ypPurchaseAmount;

    @Schema(description = "本季度中药饮片总销售额")
    @ExcelProperty("本季度中药饮片总销售额")
    private BigDecimal ypSellAmount;

    @Schema(description = "本季度中药颗粒剂总采购额")
    @ExcelProperty("本季度中药颗粒剂总采购额")
    private BigDecimal klPurchaseAmount;

    @Schema(description = "本季度中药颗粒剂总销售额")
    @ExcelProperty("本季度中药颗粒剂总销售额")
    private BigDecimal klSellAmount;

    @Schema(description = "数据上报日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("数据上报日期")
    private String uploadDate;

    @Schema(description = "导入批次号")
    @ExcelProperty("导入批次号")
    private String importBatchNo;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}