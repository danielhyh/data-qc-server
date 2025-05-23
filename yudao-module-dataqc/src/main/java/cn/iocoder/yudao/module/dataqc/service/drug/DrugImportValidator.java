package cn.iocoder.yudao.module.dataqc.service.drug;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.drug.DrugInoutInfoDO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.drug.DrugListDO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.drug.DrugUseInfoDO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * 药品数据导入校验器
 */
@Component
public class DrugImportValidator {
    
    // 日期格式正则
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{8}$");
    
    /**
     * 校验药品目录数据
     */
    public String validateDrugList(DrugListDO drug, int rowNum) {
        StringBuilder errors = new StringBuilder();
        
        // 必填字段校验
        if (StrUtil.isEmpty(drug.getDomainCode())) {
            errors.append("系统编码不能为空；");
        }
        if (StrUtil.isEmpty(drug.getOrganizationCode())) {
            errors.append("组织机构代码不能为空；");
        }
        if (StrUtil.isEmpty(drug.getHospitalCode())) {
            errors.append("医疗机构代码不能为空；");
        }
        if (StrUtil.isEmpty(drug.getHosDrugId())) {
            errors.append("院内药品编码不能为空；");
        }
        if (StrUtil.isEmpty(drug.getYpid())) {
            errors.append("国家药管平台编码不能为空；");
        }
        if (StrUtil.isEmpty(drug.getDrugName())) {
            errors.append("品种通用名不能为空；");
        }
        if (StrUtil.isEmpty(drug.getProductName())) {
            errors.append("产品通用名不能为空；");
        }
        
        // 数值字段校验
        if (drug.getDrugFactor() == null || drug.getDrugFactor().compareTo(BigDecimal.ZERO) <= 0) {
            errors.append("转换系数必须大于0；");
        }
        
        // 字典值校验
        if (!isValidFlag(drug.getUnityPurchaseFlag())) {
            errors.append("是否集中采购只能填1或2；");
        }
        if (!isValidFlag(drug.getBaseFlag())) {
            errors.append("是否基本药物只能填1或2；");
        }
        if (!isValidFlag(drug.getUniformityFlag())) {
            errors.append("是否一致性评价只能填1或2；");
        }
        
        if (errors.length() > 0) {
            return "第" + rowNum + "行：" + errors.toString();
        }
        return null;
    }
    
    /**
     * 校验出入库数据
     */
    public String validateInoutInfo(DrugInoutInfoDO inout, int rowNum) {
        StringBuilder errors = new StringBuilder();
        
        // 必填字段校验
        if (StrUtil.isEmpty(inout.getHosDrugId())) {
            errors.append("院内药品编码不能为空；");
        }
        if (StrUtil.isEmpty(inout.getOutInDate()) || !isValidDate(inout.getOutInDate())) {
            errors.append("出入库日期格式错误(应为yyyyMMdd)；");
        }
        
        // 入库数据校验
        if ("IN".equals(inout.getIoType())) {
            if (inout.getInPackQuantity() == null || inout.getInPackQuantity() <= 0) {
                errors.append("入库数量必须大于0；");
            }
            if (inout.getInPackPrice() == null || 
                    inout.getInPackPrice().compareTo(BigDecimal.ZERO) <= 0) {
                errors.append("入库价格必须大于0；");
            }
        }
        
        // 出库数据校验
        if ("OUT".equals(inout.getIoType())) {
            if (inout.getOutPackQuantity() == null || inout.getOutPackQuantity() <= 0) {
                errors.append("出库数量必须大于0；");
            }
        }
        
        if (errors.length() > 0) {
            return "第" + rowNum + "行：" + errors.toString();
        }
        return null;
    }
    
    /**
     * 校验使用情况数据
     */
    public String validateUseInfo(DrugUseInfoDO use, int rowNum) {
        StringBuilder errors = new StringBuilder();
        
        // 必填字段校验
        if (StrUtil.isEmpty(use.getHosDrugId())) {
            errors.append("院内药品编码不能为空；");
        }
        if (StrUtil.isEmpty(use.getSellDate()) || !isValidDate(use.getSellDate())) {
            errors.append("销售日期格式错误(应为yyyyMMdd)；");
        }
        if (use.getSellPackQuantity() == null || use.getSellPackQuantity() <= 0) {
            errors.append("销售数量必须大于0；");
        }
        if (use.getSellPackPrice() == null || 
                use.getSellPackPrice().compareTo(BigDecimal.ZERO) <= 0) {
            errors.append("销售价格必须大于0；");
        }
        
        if (!errors.isEmpty()) {
            return "第" + rowNum + "行：" + errors;
        }
        return null;
    }
    
    /**
     * 校验是否标志
     */
    private boolean isValidFlag(String flag) {
        return "1".equals(flag) || "2".equals(flag);
    }
    
    /**
     * 校验日期格式
     */
    private boolean isValidDate(String date) {
        return DATE_PATTERN.matcher(date).matches();
    }
}