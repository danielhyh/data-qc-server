package cn.iocoder.yudao.module.dataqc.dal.dataobject.importlog;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 数据导入日志 DO
 *
 * @author 管理员
 */
@TableName("system_import_log")
@KeySequence("system_import_log_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportLogDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;
    /**
     * 批次号
     */
    private String batchNo;
    /**
     * 文件名
     */
    private String fileName;
    /**
     * 文件类型
     */
    private String fileType;
    /**
     * 目标表名
     */
    private String tableName;
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
     * 状态(PROCESSING-处理中,SUCCESS-成功,FAIL-失败)
     */
    private String status;
    /**
     * 错误信息
     */
    private String errorMsg;
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    /**
     * 操作人ID
     */
    private Long operatorId;
    /**
     * 操作人姓名
     */
    private String operatorName;


}