package cn.iocoder.yudao.module.drug.dal.dataobject.batch;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 药品数据导入任务 DO
 *
 * @author hyh
 */
@TableName("drug_import_task")
@KeySequence("drug_import_task_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportTaskDO extends BaseDO {

    /**
     * 任务ID
     */
    @TableId
    private Long id;
    /**
     * 任务编号（格式：DRUG_YYYYMMDD_XXXXXX）
     */
    private String taskNo;
    /**
     * 任务名称
     */
    private String taskName;
    /**
     * 导入类型:1-单文件,2-压缩包
     */
    private Integer importType;
    /**
     * 原始文件名称
     */
    private String fileName;
    /**
     * 文件存储路径
     */
    private String filePath;
    /**
     * 文件大小(字节)
     */
    private Long fileSize;
    /**
     * 解压后的文件列表(JSON格式)
     */
    private String extractedFiles;
    /**
     * 任务状态:0-待处理,1-解压中,2-数据导入中,3-质控中,4-完成,5-失败,6-部分成功
     */
    private Integer status;
    /**
     * 解压状态:0-未开始,1-进行中,2-成功,3-失败
     */
    private Integer extractStatus;
    /**
     * 导入状态:0-未开始,1-进行中,2-成功,3-失败
     */
    private Integer importStatus;
    /**
     * 质控状态:0-未开始,1-进行中,2-成功,3-失败
     */
    private Integer qcStatus;
    /**
     * 预期文件数量
     */
    private Integer totalFiles;
    /**
     * 成功文件数
     */
    private Integer successFiles;
    /**
     * 失败文件数
     */
    private Integer failedFiles;
    /**
     * 总记录数
     */
    private Long totalRecords;
    /**
     * 成功记录数
     */
    private Long successRecords;
    /**
     * 失败记录数
     */
    private Long failedRecords;
    /**
     * 整体进度百分比
     */
    private BigDecimal progressPercent;
    /**
     * 各表处理进度(JSON格式)
     */
    private String tableProgress;
    /**
     * 开始处理时间
     */
    private LocalDateTime startTime;
    /**
     * 解压完成时间
     */
    private LocalDateTime extractEndTime;
    /**
     * 导入完成时间
     */
    private LocalDateTime importEndTime;
    /**
     * 质控完成时间
     */
    private LocalDateTime qcEndTime;
    /**
     * 任务结束时间
     */
    private LocalDateTime endTime;
    /**
     * 错误信息
     */
    private String errorMessage;
    /**
     * 详细错误信息(JSON格式)
     */
    private String errorDetail;


}