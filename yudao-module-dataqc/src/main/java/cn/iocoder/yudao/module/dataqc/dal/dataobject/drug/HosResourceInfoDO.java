package cn.iocoder.yudao.module.dataqc.dal.dataobject.drug;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
     * 实有床位数
     */
    private Integer bedsNum;
    /**
     * 执业医师数
     */
    private Integer pracDockerNum;
    /**
     * 执业助理医师数
     */
    private Integer assDockerNum;
    /**
     * 总诊疗人次数
     */
    private Integer visitCount;
    /**
     * 出院人数
     */
    private Integer leaveHosCount;
    /**
     * 本季度药品总收入(元)
     */
    private BigDecimal drugSellAmount;
    /**
     * 本季度中药饮片总采购额
     */
    private BigDecimal ypPurchaseAmount;
    /**
     * 本季度中药饮片总销售额
     */
    private BigDecimal ypSellAmount;
    /**
     * 本季度中药颗粒剂总采购额
     */
    private BigDecimal klPurchaseAmount;
    /**
     * 本季度中药颗粒剂总销售额
     */
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