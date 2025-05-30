package cn.iocoder.yudao.module.drug.controller.admin.batch.vo;

import cn.iocoder.yudao.module.drug.enums.TableTypeEnum;
import lombok.Data;
import org.apache.tomcat.jni.FileInfo;

@Data
public class FileExtractResult {
    private boolean success;
    private String message;
    private String errorMessage;
    private Object fileInfos;

    public FileInfo getFileInfo(TableTypeEnum tableType) {
        return null;
    }
}
