package cn.iocoder.yudao.module.dataqc.dal.dataobject.batchimport;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 批量导入任务 DO
 *
 * @author 芋道源码
 */
@TableName("system_batch_import_task")
@KeySequence("system_batch_import_task_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchImportTaskDO extends BaseDO {

    /**
     * 任务ID
     */
    @TableId
    private Long id;
    /**
     * 任务编号
     */
    private String taskNo;
    /**
     * 任务名称
     */
    private String taskName;
    /**
     * 上传文件名
     */
    private String fileName;
    /**
     * 文件存储路径
     */
    private String filePath;
    /**
     * 状态(0-待处理,1-处理中,2-成功,3-失败,4-部分成功)
     */
    private Integer status;
    /**
     * 文件总数
     */
    private Integer totalFiles;
    /**
     * 成功文件数
     */
    private Integer successFiles;
    /**
     * 失败文件数
     */
    private Integer failFiles;
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
     * 结果详情(JSON格式)
     */
    private String resultDetail;


}