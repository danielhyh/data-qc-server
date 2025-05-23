package cn.iocoder.yudao.module.dataqc.dal.dataobject.batchimport;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 批量导入任务明细 DO
 *
 * @author 芋道源码
 */
@TableName("system_batch_import_task_detail")
@KeySequence("system_batch_import_task_detail_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchImportTaskDetailDO extends BaseDO {

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
     * 文件类型(HOSPITAL_INFO,DRUG_LIST,DRUG_IN,DRUG_OUT,DRUG_USE)
     */
    private String fileType;
    /**
     * 文件名
     */
    private String fileName;
    /**
     * 目标表名
     */
    private String tableName;
    /**
     * 状态(0-待处理,1-处理中,2-成功,3-失败)
     */
    private Integer status;
    /**
     * 总行数
     */
    private Integer totalRows;
    /**
     * 成功行数
     */
    private Integer successRows;
    /**
     * 失败行数
     */
    private Integer failRows;
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    /**
     * 错误信息
     */
    private String errorMsg;
    /**
     * 错误详情(JSON格式)
     */
    private String errorDetail;
    /**
     * 导入批次号
     */
    private String importBatchNo;


}