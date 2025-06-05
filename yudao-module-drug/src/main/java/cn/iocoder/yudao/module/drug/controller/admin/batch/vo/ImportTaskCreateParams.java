package cn.iocoder.yudao.module.drug.controller.admin.batch.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportTaskCreateParams {
    
    @NotBlank(message = "任务名称不能为空")
    @Length(min = 2, max = 100, message = "任务名称长度在 2 到 100 个字符")
    private String taskName;
    
    @Length(max = 500, message = "备注说明不能超过 500 个字符")
    private String description;
    
    @Length(max = 50, message = "数据来源不能超过 50 个字符")
    private String dataSource;
}