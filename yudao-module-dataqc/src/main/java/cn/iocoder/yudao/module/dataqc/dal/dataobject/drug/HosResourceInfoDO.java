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

/**
 * 医疗机构资源信息 DO
 *
 * @author 管理员
 */
@TableName("gh_hos_resource_info")
@KeySequence("gh_hos_resource_info_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HosResourceInfoDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;
    /**
     * 系统编码
     */
    private String domainCode;
    @Schema(description = "省级行政区划代码")
    private String provinceCode;

    @Schema(description = "年度药品总收入（元）")
    private BigDecimal annualDrugIncome;
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
     * 统计日期(yyyyMMdd)
     */
    private String statDate;
    /**
     * 在实有床位数字段添加转换器
     */
    @ExcelProperty(value = "实有床位数", converter = ExcelDataConverter.SafeLongConverter.class)
    private Integer bedsNum;

    /**
     * 在执业医师数字段添加转换器
     */
    @ExcelProperty(value = "执业医师数", converter = ExcelDataConverter.SafeLongConverter.class)
    private Integer pracDockerNum;

    /**
     * 在执业助理医师数字段添加转换器
     */
    @ExcelProperty(value = "执业助理医师数", converter = ExcelDataConverter.SafeLongConverter.class)
    private Integer assDockerNum;

    /**
     * 在总诊疗人次数字段添加转换器
     */
    @ExcelProperty(value = "总诊疗人次数", converter = ExcelDataConverter.SafeLongConverter.class)
    private Integer visitCount;

    /**
     * 在出院人数字段添加转换器
     */
    @ExcelProperty(value = "出院人数", converter = ExcelDataConverter.SafeLongConverter.class)
    private Integer leaveHosCount;

    /**
     * 在药品总收入字段添加转换器
     */
    @ExcelProperty(value = "本季度药品总收入(元)", converter = ExcelDataConverter.SafeBigDecimalConverter.class)
    private BigDecimal drugSellAmount;

    /**
     * 在中药饮片总采购额字段添加转换器
     */
    @ExcelProperty(value = "本季度中药饮片总采购额", converter = ExcelDataConverter.SafeBigDecimalConverter.class)
    private BigDecimal ypPurchaseAmount;

    /**
     * 在中药饮片总销售额字段添加转换器
     */
    @ExcelProperty(value = "本季度中药饮片总销售额", converter = ExcelDataConverter.SafeBigDecimalConverter.class)
    private BigDecimal ypSellAmount;
    /**
     * 本季度中药颗粒剂总采购额
     */
    @ExcelProperty(value = "本季度中药颗粒剂总采购额", converter = ExcelDataConverter.SafeBigDecimalConverter.class)
    private BigDecimal klPurchaseAmount;
    /**
     * 本季度中药颗粒剂总销售额
     */
    @ExcelProperty(value = "本季度中药颗粒剂总销售额", converter = ExcelDataConverter.SafeBigDecimalConverter.class)
    private BigDecimal klSellAmount;
    /**
     * 数据上报日期
     */
    private String uploadDate;
    /**
     * 导入批次号
     */
    private String importBatchNo;


}