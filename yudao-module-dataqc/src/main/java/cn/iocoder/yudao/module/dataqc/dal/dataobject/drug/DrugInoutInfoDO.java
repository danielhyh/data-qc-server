package cn.iocoder.yudao.module.dataqc.dal.dataobject.drug;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.dataqc.util.ExcelDataConverter;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 药品出入库 DO
 *
 * @author 管理员
 */
@TableName("gh_drug_inout_info")
@KeySequence("gh_drug_inout_info_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrugInoutInfoDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;
    /**
     * 流水号
     */
    private Long serialNum;
    /**
     * 系统编码
     */
    private String domainCode;
    @Schema(description = "省级行政区划代码") //+
    private String provinceCode;

    @Schema(description = "入库总金额(元)", requiredMode = Schema.RequiredMode.REQUIRED, example = "26800")
    private BigDecimal inTotalPrice;
    /**
     * 组织机构代码
     */
    private String organizationCode;
    /**
     * 组织机构名称
     */
    private String organizationName;
    /**
     * 医疗机构代码
     */
    private String hospitalCode;
    /**
     * 数据上报日期
     */
    private String uploadDate;
    /**
     * 国家药管平台药品编码
     */
    private String ypid;
    /**
     * 省级药品集中采购平台药品编码
     */
    private String prDrugId;
    /**
     * 院内药品唯一码
     */
    private String hosDrugId;
    /**
     * 产品通用名
     */
    private String productName;
    /**
     * 出入库时间(yyyyMMdd)
     */
    private String outInDate;
    /**
     * 出入库类型(IN-入库,OUT-出库)
     */
    private String ioType;
    @ExcelProperty(value = "入库数量(最小销售包装单位)", converter = ExcelDataConverter.SafeLongConverter.class)
    private Long inPackQuantity;

    @ExcelProperty(value = "入库数量(最小制剂单位)", converter = ExcelDataConverter.SafeLongConverter.class)
    private Long inDosageQuantity;
    @ExcelProperty(value = "入库价格(最小销售包装单位)", converter = ExcelDataConverter.SafeBigDecimalConverter.class)
    private BigDecimal inPackPrice;
    @ExcelProperty(value = "入库价格(最小制剂单位)", converter = ExcelDataConverter.SafeBigDecimalConverter.class)
    private BigDecimal inDosagePrice;
    @ExcelProperty(value = "出库数量(最小销售包装单位)", converter = ExcelDataConverter.SafeLongConverter.class)
    private Long outPackQuantity;

    @ExcelProperty(value = "出库数量(最小制剂单位)", converter = ExcelDataConverter.SafeLongConverter.class)
    private Long outDosageQuantity;
    /**
     * 供应商代码
     */
    private String supplierCode;
    /**
     * 供应商名称
     */
    private String supplierName;
    /**
     * 批号
     */
    private String batchNo;
    /**
     * 生产日期
     */
    private String productionDate;
    /**
     * 有效期至
     */
    private String expiryDate;
    /**
     * 导入批次号
     */
    private String importBatchNo;
    /**
     * 导入时间
     */
    private LocalDateTime importTime;
    /**
     * 数据来源(IN_IMPORT-入库导入,OUT_IMPORT-出库导入)
     */
    private String sourceType;


}