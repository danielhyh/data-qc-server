package cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 药品出入库统计 VO
 * <p>
 * 这个类用于封装药品出入库的统计数据，包括入库和出库的各项指标统计
 */
@Schema(description = "管理后台 - 药品出入库统计 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ExcelIgnoreUnannotated
public class InoutStatVO {

    /**
     * 入库统计相关字段
     */
    @Schema(description = "入库次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "150")
    @ExcelProperty("入库次数")
    private Long inCount;

    @Schema(description = "入库总数量(最小销售包装单位)", requiredMode = Schema.RequiredMode.REQUIRED, example = "5000")
    @ExcelProperty("入库总数量")
    private Long inQuantity;

    @Schema(description = "入库总金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "125000.50")
    @ExcelProperty("入库总金额")
    private BigDecimal inAmount;

    /**
     * 出库统计相关字段
     */
    @Schema(description = "出库次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "88")
    @ExcelProperty("出库次数")
    private Long outCount;

    @Schema(description = "出库总数量(最小销售包装单位)", requiredMode = Schema.RequiredMode.REQUIRED, example = "3200")
    @ExcelProperty("出库总数量")
    private Long outQuantity;

    /**
     * 供应商统计相关字段
     */
    @Schema(description = "供应商数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "25")
    @ExcelProperty("供应商数量")
    private Long supplierCount;

    /**
     * 计算属性 - 库存余量
     * 通过入库数量减去出库数量得到当前库存余量
     */
    @Schema(description = "库存余量(最小销售包装单位)", example = "1800")
    public Long getStockBalance() {
        if (inQuantity == null || outQuantity == null) {
            return 0L;
        }
        return inQuantity - outQuantity;
    }

    /**
     * 计算属性 - 出库率
     * 出库数量占入库数量的百分比
     */
    @Schema(description = "出库率(%)", example = "64.0")
    public BigDecimal getOutboundRatio() {
        if (inQuantity == null || inQuantity == 0 || outQuantity == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(outQuantity)
                .divide(BigDecimal.valueOf(inQuantity), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * 计算属性 - 平均入库单价
     * 入库总金额除以入库总数量
     */
    @Schema(description = "平均入库单价", example = "25.01")
    public BigDecimal getAvgInPrice() {
        if (inAmount == null || inQuantity == null || inQuantity == 0) {
            return BigDecimal.ZERO;
        }
        return inAmount.divide(BigDecimal.valueOf(inQuantity), 4, BigDecimal.ROUND_HALF_UP);
    }
}