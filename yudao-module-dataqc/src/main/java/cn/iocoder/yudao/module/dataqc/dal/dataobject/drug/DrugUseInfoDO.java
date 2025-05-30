package cn.iocoder.yudao.module.dataqc.dal.dataobject.drug;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 药品使用情况 DO
 *
 * @author 管理员
 */
@TableName("gh_drug_use_info")
@KeySequence("gh_drug_use_info_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrugUseInfoDO extends BaseDO {

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

    @Schema(description = "销售总金额（元）", requiredMode = Schema.RequiredMode.REQUIRED, example = "13038")
    private BigDecimal sellTotalPrice;
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
     * 产品通用名
     */
    private String productName;
    /**
     * 药品销售日期(yyyyMMdd)
     */
    private String sellDate;
    private BigDecimal sellPackPrice;

    private BigDecimal sellDosagePrice;

    private Long sellPackQuantity;

    private Long sellDosageQuantity;
    /**
     * 使用科室代码
     */
    private String departmentCode;
    /**
     * 使用科室名称
     */
    private String departmentName;
    /**
     * 开具医生代码
     */
    private String doctorCode;
    /**
     * 开具医生姓名
     */
    private String doctorName;
    /**
     * 患者类型(门诊/住院)
     */
    private String patientType;
    /**
     * 导入批次号
     */
    private String importBatchNo;
    /**
     * 导入时间
     */
    private LocalDateTime importTime;


}