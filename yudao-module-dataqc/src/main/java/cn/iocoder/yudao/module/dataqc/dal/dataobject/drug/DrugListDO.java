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
 * 药品目录 DO
 *
 * @author 管理员
 */
@TableName("gh_drug_list")
@KeySequence("gh_drug_list_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrugListDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;
    /**
     * 流水号
     */
    private Long serialNum;
    @Schema(description = "省级行政区划代码") //+
    private String provinceCode;

    @Schema(description = "医疗机构名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String hospitalName;
    /**
     * 系统编码
     */
    private String domainCode;
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
     * 批准文号
     */
    private String approvalNum;
    /**
     * 品种通用名
     */
    private String drugName;
    /**
     * 产品通用名
     */
    private String productName;
    /**
     * 商品名
     */
    private String tradeName;
    /**
     * 商品名(英文)
     */
    private String tradeEngName;
    /**
     * 生产企业
     */
    private String manufacturer;
    /**
     * 剂型名称
     */
    private String drugForm;
    /**
     * 制剂规格
     */
    private String drugSpec;
    /**
     * 制剂单位
     */
    private String dosageUnit;
    /**
     * 最小销售包装单位
     */
    private String packUnit;
    /**
     * 转换系数
     */
    @ExcelProperty(value = "转换系数", converter = ExcelDataConverter.SafeBigDecimalConverter.class)
    private BigDecimal drugFactor;
    /**
     * 是否网上集中采购药品(1是2否)
     */
    private String unityPurchaseFlag;
    /**
     * 是否基本药物(1是2否)
     */
    private String baseFlag;
    /**
     * 是否通过一致性评价(1是2否)
     */
    private String uniformityFlag;
    /**
     * 导入批次号
     */
    private String importBatchNo;
    /**
     * 导入时间
     */
    private LocalDateTime importTime;


}