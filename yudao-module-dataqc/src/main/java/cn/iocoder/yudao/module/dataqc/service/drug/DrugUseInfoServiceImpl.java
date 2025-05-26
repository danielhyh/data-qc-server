package cn.iocoder.yudao.module.dataqc.service.drug;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.DrugUseInfoPageReqVO;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.DrugUseInfoSaveReqVO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.drug.DrugListDO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.drug.DrugUseInfoDO;
import cn.iocoder.yudao.module.dataqc.dal.mysql.drug.DrugUseInfoMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.dataqc.enums.ErrorCodeConstants.*;
import static com.baomidou.mybatisplus.extension.toolkit.Db.list;
import static com.baomidou.mybatisplus.extension.toolkit.Db.saveBatch;

/**
 * 药品使用情况 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
@Slf4j
public class DrugUseInfoServiceImpl implements DrugUseInfoService {

    @Resource
    private DrugUseInfoMapper drugUseInfoMapper;
    @Resource
    private DrugListService drugListService;

    @Override
    public Long createDrugUseInfo(DrugUseInfoSaveReqVO createReqVO) {
        // 插入
        DrugUseInfoDO drugUseInfo = BeanUtils.toBean(createReqVO, DrugUseInfoDO.class);
        drugUseInfoMapper.insert(drugUseInfo);
        // 返回
        return drugUseInfo.getId();
    }

    @Override
    public void updateDrugUseInfo(DrugUseInfoSaveReqVO updateReqVO) {
        // 校验存在
        validateDrugUseInfoExists(updateReqVO.getId());
        // 更新
        DrugUseInfoDO updateObj = BeanUtils.toBean(updateReqVO, DrugUseInfoDO.class);
        drugUseInfoMapper.updateById(updateObj);
    }

    @Override
    public void deleteDrugUseInfo(Long id) {
        // 校验存在
        validateDrugUseInfoExists(id);
        // 删除
        drugUseInfoMapper.deleteById(id);
    }

    @Override
        public void deleteDrugUseInfoListByIds(List<Long> ids) {
        // 校验存在
        validateDrugUseInfoExists(ids);
        // 删除
        drugUseInfoMapper.deleteByIds(ids);
        }

    private void validateDrugUseInfoExists(List<Long> ids) {
        List<DrugUseInfoDO> list = drugUseInfoMapper.selectByIds(ids);
        if (CollUtil.isEmpty(list) || list.size() != ids.size()) {
            throw exception(DRUG_USE_INFO_NOT_EXISTS);
        }
    }

    private void validateDrugUseInfoExists(Long id) {
        if (drugUseInfoMapper.selectById(id) == null) {
            throw exception(DRUG_USE_INFO_NOT_EXISTS);
        }
    }

    @Override
    public DrugUseInfoDO getDrugUseInfo(Long id) {
        return drugUseInfoMapper.selectById(id);
    }

    @Override
    public PageResult<DrugUseInfoDO> getDrugUseInfoPage(DrugUseInfoPageReqVO pageReqVO) {
        return drugUseInfoMapper.selectPage(pageReqVO);
    }

    @Override
    public List<DrugUseInfoDO> selectUseList(DrugUseInfoPageReqVO queryVO) {
        LambdaQueryWrapper<DrugUseInfoDO> wrapper = new LambdaQueryWrapper<>();

        // 构建查询条件 - 这展示了如何处理动态查询
        wrapper.eq(StrUtil.isNotEmpty(queryVO.getHospitalCode()),
                DrugUseInfoDO::getHospitalCode, queryVO.getHospitalCode());
        wrapper.eq(StrUtil.isNotEmpty(queryVO.getHosDrugId()),
                DrugUseInfoDO::getHosDrugId, queryVO.getHosDrugId());
        wrapper.like(StrUtil.isNotEmpty(queryVO.getProductName()),
                DrugUseInfoDO::getProductName, queryVO.getProductName());
        wrapper.eq(StrUtil.isNotEmpty(queryVO.getDepartmentCode()),
                DrugUseInfoDO::getDepartmentCode, queryVO.getDepartmentCode());
        wrapper.eq(StrUtil.isNotEmpty(queryVO.getPatientType()),
                DrugUseInfoDO::getPatientType, queryVO.getPatientType());

        // 日期范围查询
        if (StrUtil.isNotEmpty(queryVO.getBeginDate()) && StrUtil.isNotEmpty(queryVO.getEndDate())) {
            wrapper.between(DrugUseInfoDO::getSellDate, queryVO.getBeginDate(), queryVO.getEndDate());
        }

        wrapper.orderByDesc(DrugUseInfoDO::getSellDate);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importUseData(MultipartFile file, boolean updateSupport) throws Exception {
        String batchNo = generateBatchNo();

        try {
            List<DrugUseInfoDO> dataList = ExcelUtils.read(file, DrugUseInfoDO.class);

            if (CollUtil.isEmpty(dataList)) {
                throw exception(IMPORT_DATA_CANNOT_BE_EMPTY);
            }

            List<DrugUseInfoDO> insertList = new ArrayList<>();
            List<String> errorMsgs = new ArrayList<>();

            for (int i = 0; i < dataList.size(); i++) {
                DrugUseInfoDO useInfo = dataList.get(i);

                // 数据校验
                String errorMsg = validateUseData(useInfo, i + 1);
                if (StrUtil.isNotEmpty(errorMsg)) {
                    errorMsgs.add(errorMsg);
                    continue;
                }

                // 验证药品是否存在 - 这是业务规则校验的重要例子
                DrugListDO drug = drugListService.selectByHosDrugId(useInfo.getHosDrugId());
                if (drug == null) {
                    errorMsgs.add("第" + (i + 1) + "行：药品编码[" + useInfo.getHosDrugId() + "]不存在");
                    continue;
                }

                // 自动填充关联数据
                useInfo.setYpid(drug.getYpid());
                useInfo.setPrDrugId(drug.getPrDrugId());
                useInfo.setProductName(drug.getProductName());

                // 计算制剂单位的数量和价格
                if (drug.getDrugFactor() != null && drug.getDrugFactor().compareTo(BigDecimal.ZERO) > 0) {
                    useInfo.setSellDosageQuantity(
                            useInfo.getSellPackQuantity() * drug.getDrugFactor().longValue());
                    useInfo.setSellDosagePrice(
                            useInfo.getSellPackPrice().divide(drug.getDrugFactor(), 2, BigDecimal.ROUND_HALF_UP));
                }

                // 设置批次信息
                useInfo.setImportBatchNo(batchNo);
                useInfo.setImportTime(LocalDateTime.now());
                useInfo.setUploadDate(DateUtil.format(DateUtil.date(), "yyyyMMdd"));

                insertList.add(useInfo);
            }

            // 批量保存
            if (!insertList.isEmpty()) {
                saveBatch(insertList);
            }

            return String.format("导入成功！总数：%d，成功：%d，失败：%d",
                    dataList.size(), insertList.size(), errorMsgs.size());

        } catch (Exception e) {
            log.error("导入药品使用数据失败", e);
            throw exception(IMPORT_FAILED, e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> getUseStatistics(DrugUseInfoPageReqVO queryVO) {
        // 这个方法展示了如何进行数据统计分析
        return drugUseInfoMapper.selectUseStatistics(queryVO);
    }

    @Override
    public List<Map<String, Object>> getDepartmentRanking(String startDate, String endDate) {
        // 科室用药排名 - 展示了如何进行排名统计
        return drugUseInfoMapper.selectDepartmentRanking(startDate, endDate);
    }

    @Override
    public List<Map<String, Object>> getDrugUseRanking(String startDate, String endDate) {
        // 药品使用排名
        return drugUseInfoMapper.selectDrugUseRanking(startDate, endDate, 20); // 取前20名
    }

    @Override
    public Map<String, Object> getBaseDrugAnalysis(String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();

        // 查询基药总金额
        BigDecimal baseDrugAmount = drugUseInfoMapper.selectBaseDrugAmount(startDate, endDate);
        result.put("baseDrugAmount", baseDrugAmount != null ? baseDrugAmount : BigDecimal.ZERO);

        // 查询非基药总金额
        BigDecimal nonBaseDrugAmount = drugUseInfoMapper.selectNonBaseDrugAmount(startDate, endDate);
        result.put("nonBaseDrugAmount", nonBaseDrugAmount != null ? nonBaseDrugAmount : BigDecimal.ZERO);

        return result;
    }

    // 私有辅助方法
    private String generateBatchNo() {
        return "USE_" + DateUtil.format(DateUtil.date(), "yyyyMMddHHmmss") + "_" + RandomUtil.randomNumbers(4);
    }

    private String validateUseData(DrugUseInfoDO data, int rowNum) {
        StringBuilder errors = new StringBuilder();

        if (StrUtil.isEmpty(data.getHosDrugId())) {
            errors.append("药品编码不能为空；");
        }
        if (StrUtil.isEmpty(data.getSellDate()) || !isValidDate(data.getSellDate())) {
            errors.append("销售日期格式错误；");
        }
        if (data.getSellPackQuantity() == null || data.getSellPackQuantity() <= 0) {
            errors.append("销售数量必须大于0；");
        }
        if (data.getSellPackPrice() == null || data.getSellPackPrice().compareTo(BigDecimal.ZERO) <= 0) {
            errors.append("销售价格必须大于0；");
        }

        if (errors.length() > 0) {
            return "第" + rowNum + "行：" + errors.toString();
        }
        return null;
    }

    private boolean isValidDate(String date) {
        return date != null && date.matches("^\\d{8}$");
    }

}