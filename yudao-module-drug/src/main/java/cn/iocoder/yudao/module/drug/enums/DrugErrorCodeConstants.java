package cn.iocoder.yudao.module.drug.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * 药品监测质量控制平台错误码常量
 * <p>
 * 错误码格式: 1-003-xxx-xxx (药品模块使用1-003段)
 * <p>
 * 模块分配:
 * - 000: 任务管理相关错误
 * - 001: 压缩包处理相关错误
 * - 002: 数据导入相关错误
 * - 003: 质控相关错误
 * - 004: YPID管理相关错误
 * - 005: 数据治理相关错误
 * - 006: 文件处理相关错误
 * - 007: 系统配置相关错误
 */
public interface DrugErrorCodeConstants {

    // ========== 任务管理模块 1-003-000-000 ==========
    ErrorCode TASK_CREATE_FAILED = new ErrorCode(1_003_000_000, "任务创建失败");
    ErrorCode TASK_NOT_FOUND = new ErrorCode(1_003_000_001, "任务不存在");
    ErrorCode TASK_STATUS_INVALID = new ErrorCode(1_003_000_002, "任务状态异常，无法执行此操作");
    ErrorCode TASK_RETRY_FAILED = new ErrorCode(1_003_000_003, "任务重试失败");
    ErrorCode TASK_RETRY_LIMIT_EXCEEDED = new ErrorCode(1_003_000_004, "任务重试次数超过限制");
    ErrorCode TASK_CANCEL_FAILED = new ErrorCode(1_003_000_005, "任务取消失败");
    ErrorCode TASK_ALREADY_COMPLETED = new ErrorCode(1_003_000_006, "任务已完成，无法重复执行");
    ErrorCode TASK_PROCESSING = new ErrorCode(1_003_000_007, "任务正在处理中，请稍后");
    ErrorCode TASK_PERMISSION_DENIED = new ErrorCode(1_003_000_008, "无权限操作此任务");

    // ========== 压缩包处理模块 1-003-001-000 ==========
    ErrorCode ZIP_FORMAT_UNSUPPORTED = new ErrorCode(1_003_001_000, "压缩包格式不支持，仅支持.zip和.rar格式");
    ErrorCode ZIP_EXTRACT_FAILED = new ErrorCode(1_003_001_001, "压缩包解压失败，原因：{}");
    ErrorCode ZIP_FILE_INCOMPLETE = new ErrorCode(1_003_001_002, "压缩包文件不完整，缺少必要的Excel文件");
    ErrorCode ZIP_FILE_TOO_LARGE = new ErrorCode(1_003_001_003, "压缩包文件过大，超过最大限制{}MB");
    ErrorCode ZIP_PASSWORD_REQUIRED = new ErrorCode(1_003_001_004, "压缩包需要密码");
    ErrorCode ZIP_CORRUPTED = new ErrorCode(1_003_001_005, "压缩包文件损坏");
    ErrorCode ZIP_NESTED_NOT_SUPPORTED = new ErrorCode(1_003_001_006, "不支持嵌套压缩包");
    ErrorCode ZIP_EMPTY = new ErrorCode(1_003_001_007, "压缩包为空");

    // ========== 数据导入模块 1-003-002-000 ==========
    ErrorCode IMPORT_TABLE_FORMAT_ERROR = new ErrorCode(1_003_002_000, "表数据格式错误：{}");
    ErrorCode IMPORT_RELATION_ERROR = new ErrorCode(1_003_002_001, "表间数据关联错误：{}");
    ErrorCode IMPORT_BATCH_FAILED = new ErrorCode(1_003_002_002, "批量导入失败");
    ErrorCode IMPORT_EXCEL_PARSE_ERROR = new ErrorCode(1_003_002_003, "Excel文件解析失败：{}");
    ErrorCode IMPORT_DATA_DUPLICATE = new ErrorCode(1_003_002_004, "存在重复数据：{}");
    ErrorCode IMPORT_REQUIRED_FIELD_MISSING = new ErrorCode(1_003_002_005, "缺少必填字段：{}");
    ErrorCode IMPORT_FIELD_FORMAT_INVALID = new ErrorCode(1_003_002_006, "字段格式不正确：{}");
    ErrorCode IMPORT_DATA_TOO_LARGE = new ErrorCode(1_003_002_007, "导入数据量过大，单次最多支持{}条记录");
    ErrorCode IMPORT_TABLE_NOT_FOUND = new ErrorCode(1_003_002_008, "找不到指定的数据表：{}");
    ErrorCode IMPORT_TRANSACTION_ROLLBACK = new ErrorCode(1_003_002_009, "数据导入事务回滚，原因：{}");
    ErrorCode IMPORT_RETRY_NOT_SUPPORTED = new ErrorCode(1_003_002_010, "当前任务状态不支持重试");
    ErrorCode IMPORT_TASK_LOCKED = new ErrorCode(1_003_002_011, "任务正在被其他用户操作，请稍后重试");
    ErrorCode IMPORT_RETRY_TYPE_UNSUPPORTED = new ErrorCode(1_003_002_012, "不支持的重试类型：{}");

    // ========== 质控模块 1-003-003-000 ==========
    ErrorCode QC_RULE_EXECUTE_FAILED = new ErrorCode(1_003_003_000, "质控规则执行失败：{}");
    ErrorCode QC_TABLE_CHECK_FAILED = new ErrorCode(1_003_003_001, "表级质控未通过：{}");
    ErrorCode QC_CROSS_TABLE_FAILED = new ErrorCode(1_003_003_002, "跨表质控失败：{}");
    ErrorCode QC_RULE_NOT_FOUND = new ErrorCode(1_003_003_003, "质控规则不存在");
    ErrorCode QC_RULE_DISABLED = new ErrorCode(1_003_003_004, "质控规则已禁用");
    ErrorCode QC_CONFIG_ERROR = new ErrorCode(1_003_003_005, "质控配置错误：{}");
    ErrorCode QC_EXPRESSION_INVALID = new ErrorCode(1_003_003_006, "质控规则表达式无效：{}");
    ErrorCode QC_RESULT_SAVE_FAILED = new ErrorCode(1_003_003_007, "质控结果保存失败");
    ErrorCode QC_REPORT_GENERATE_FAILED = new ErrorCode(1_003_003_008, "质控报告生成失败");

    // ========== YPID管理模块 1-003-004-000 ==========
    ErrorCode YPID_MATCH_SERVICE_ERROR = new ErrorCode(1_003_004_000, "YPID匹配服务异常");
    ErrorCode YPID_CODE_FORMAT_ERROR = new ErrorCode(1_003_004_001, "YPID编码格式错误：{}");
    ErrorCode YPID_NOT_FOUND = new ErrorCode(1_003_004_002, "未找到匹配的YPID编码");
    ErrorCode YPID_BATCH_MATCH_FAILED = new ErrorCode(1_003_004_003, "批量YPID匹配失败");
    ErrorCode YPID_MANUAL_MATCH_INVALID = new ErrorCode(1_003_004_004, "手动匹配的YPID编码无效");
    ErrorCode YPID_ALREADY_MATCHED = new ErrorCode(1_003_004_005, "该记录已完成YPID匹配");
    ErrorCode YPID_DICTIONARY_UPDATE_FAILED = new ErrorCode(1_003_004_006, "YPID字典更新失败");

    // ========== 数据治理模块 1-003-005-000 ==========
    ErrorCode GOVERNANCE_ANALYZE_FAILED = new ErrorCode(1_003_005_000, "异常数据分析失败");
    ErrorCode GOVERNANCE_REPAIR_STRATEGY_NOT_FOUND = new ErrorCode(1_003_005_001, "修复策略不存在：{}");
    ErrorCode GOVERNANCE_AUTO_REPAIR_FAILED = new ErrorCode(1_003_005_002, "自动修复失败：{}");
    ErrorCode GOVERNANCE_MANUAL_REPAIR_INVALID = new ErrorCode(1_003_005_003, "手动修复数据无效：{}");
    ErrorCode GOVERNANCE_BATCH_REPAIR_PARTIAL_FAILED = new ErrorCode(1_003_005_004, "批量修复部分失败，成功{}条，失败{}条");
    ErrorCode GOVERNANCE_REPAIR_PERMISSION_DENIED = new ErrorCode(1_003_005_005, "无权限执行数据修复操作");

    // ========== 文件处理模块 1-003-006-000 ==========
    ErrorCode FILE_UPLOAD_FAILED = new ErrorCode(1_003_006_000, "文件上传失败");
    ErrorCode FILE_TYPE_NOT_SUPPORTED = new ErrorCode(1_003_006_001, "不支持的文件类型：{}");
    ErrorCode FILE_SIZE_EXCEEDED = new ErrorCode(1_003_006_002, "文件大小超过限制：{}MB");
    ErrorCode FILE_NOT_FOUND = new ErrorCode(1_003_006_003, "文件不存在：{}");
    ErrorCode FILE_READ_ERROR = new ErrorCode(1_003_006_004, "文件读取失败：{}");
    ErrorCode FILE_WRITE_ERROR = new ErrorCode(1_003_006_005, "文件写入失败：{}");
    ErrorCode FILE_DELETE_ERROR = new ErrorCode(1_003_006_006, "文件删除失败：{}");
    ErrorCode FILE_TEMPLATE_NOT_FOUND = new ErrorCode(1_003_006_007, "模板文件不存在");

    // ========== 系统配置模块 1-003-007-000 ==========
    ErrorCode CONFIG_RULE_DUPLICATE = new ErrorCode(1_003_007_000, "质控规则编码重复：{}");
    ErrorCode CONFIG_PARAMETER_INVALID = new ErrorCode(1_003_007_001, "配置参数无效：{}");
    ErrorCode CONFIG_UPDATE_FAILED = new ErrorCode(1_003_007_002, "配置更新失败");
    ErrorCode CONFIG_PERMISSION_DENIED = new ErrorCode(1_003_007_003, "无权限修改系统配置");
    ErrorCode CONFIG_VALIDATION_FAILED = new ErrorCode(1_003_007_004, "配置验证失败：{}");

}