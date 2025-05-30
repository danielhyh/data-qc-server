package cn.iocoder.yudao.module.drug.dal.dataobject.batch;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 药品数据导入任务明细 DO
 *
 * @author hyh
 */
@TableName("drug_import_task_detail")
@KeySequence("drug_import_task_detail_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportTaskDetailDO extends BaseDO {

    /**
     * 明细ID
     */
    @TableId
    private Long id;
    /**
     * 任务ID
     */
    private Long taskId;
    /**
     * 任务编号
     */
    private String taskNo;
    /**
     * 文件类型:HOSPITAL_INFO,DRUG_CATALOG,DRUG_INBOUND,DRUG_OUTBOUND,DRUG_USAGE
     */
    private String fileType;
    /**
     * 文件名
     */
    private String fileName;
    /**
     * 目标表名
     */
    private String targetTable;
    /**
     * 表类型:1-机构信息,2-药品目录,3-入库情况,4-出库情况,5-使用情况
     */
    private Integer tableType;
    /**
     * 状态:0-待处理,1-解析中,2-导入中,3-质控中,4-成功,5-失败
     */
    private Integer status;
    /**
     * 解析状态:0-未开始,1-进行中,2-成功,3-失败
     */
    private Integer parseStatus;
    /**
     * 导入状态:0-未开始,1-进行中,2-成功,3-失败
     */
    private Integer importStatus;
    /**
     * 质控状态:0-未开始,1-进行中,2-成功,3-失败
     */
    private Integer qcStatus;
    /**
     * 总行数
     */
    private Long totalRows;
    /**
     * 有效行数
     */
    private Long validRows;
    /**
     * 导入成功行数
     */
    private Long successRows;
    /**
     * 导入失败行数
     */
    private Long failedRows;
    /**
     * 质控通过行数
     */
    private Long qcPassedRows;
    /**
     * 质控失败行数
     */
    private Long qcFailedRows;
    /**
     * 处理进度百分比
     */
    private BigDecimal progressPercent;
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    /**
     * 解析完成时间
     */
    private LocalDateTime parseEndTime;
    /**
     * 导入完成时间
     */
    private LocalDateTime importEndTime;
    /**
     * 质控完成时间
     */
    private LocalDateTime qcEndTime;
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    /**
     * 错误信息
     */
    private String errorMessage;
    /**
     * 错误行详情(JSON格式)
     */
    private String errorRowsDetail;
    /**
     * 导入批次号
     */
    private String importBatchNo;
    /**
     * 重试次数
     */
    private Integer retryCount;
    /**
     * 最大重试次数
     */
    private Integer maxRetryCount;


}