package cn.iocoder.yudao.module.dataqc.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * 错误码枚举类 infra 系统，使用 1-002-000-000 段
 */
public interface ErrorCodeConstants {
    ErrorCode BATCH_IMPORT_TASK_DETAIL_NOT_EXISTS = new ErrorCode(1_002_000_001, "批量导入任务明细不存在");
    // 压缩包中没有找到Excel文件
    ErrorCode ZIP_FILE_NOT_FOUND_EXCEL = new ErrorCode(1_002_000_002, "压缩包中没有找到Excel文件");
    // 缺少必要文件：药品目录
    ErrorCode MISSING_NECESSARY_FILE_MEDICINE_CATEGORY = new ErrorCode(1_002_000_003, "缺少必要文件：药品目录");
    // 医疗机构资源信息不存在
    ErrorCode HOS_RESOURCE_INFO_NOT_EXISTS = new ErrorCode(1_002_000_004, "医疗机构资源信息不存在");
    // 药品目录不存在
    ErrorCode DRUG_LIST_NOT_EXISTS = new ErrorCode(1_002_000_005, "药品目录不存在");
    // 药品出入库不存在
    ErrorCode DRUG_INOUT_INFO_NOT_EXISTS = new ErrorCode(1_002_000_006, "药品出入库不存在");
    // 药品使用情况不存在
    ErrorCode DRUG_USE_INFO_NOT_EXISTS = new ErrorCode(1_002_000_007, "药品使用情况不存在");
    // 不支持的文件类型
    ErrorCode UNSUPPORTED_FILE_TYPE = new ErrorCode(1_002_000_008, "不支持的文件类型");
    // 数据导入日志不存在
    ErrorCode IMPORT_LOG_NOT_EXISTS = new ErrorCode(1_002_000_009, "数据导入日志不存在");
    // 导入数据不能为空
    ErrorCode IMPORT_DATA_CANNOT_BE_EMPTY = new ErrorCode(1_002_000_010, "导入数据不能为空");
    // 导入失败
    ErrorCode IMPORT_FAILED = new ErrorCode(1_002_000_011, "导入失败");
}
