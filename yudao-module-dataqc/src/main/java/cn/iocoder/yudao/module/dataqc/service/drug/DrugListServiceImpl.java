package cn.iocoder.yudao.module.dataqc.service.drug;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.DrugListPageReqVO;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.DrugListSaveReqVO;
import cn.iocoder.yudao.module.dataqc.controller.admin.importlog.vo.ImportLogSaveReqVO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.drug.DrugListDO;
import cn.iocoder.yudao.module.dataqc.dal.mysql.drug.DrugListMapper;
import cn.iocoder.yudao.module.dataqc.service.importlog.ImportLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.dataqc.enums.ErrorCodeConstants.*;
import static com.baomidou.mybatisplus.extension.toolkit.Db.*;

/**
 * 药品目录 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class DrugListServiceImpl implements DrugListService {

    @Resource
    private DrugListMapper drugListMapper;
    @Resource
    private ImportLogService importLogService;
    @Resource
    private DrugImportValidator validator;

    @Override
    public Long createDrugList(DrugListSaveReqVO createReqVO) {
        // 插入
        DrugListDO drugList = BeanUtils.toBean(createReqVO, DrugListDO.class);
        drugListMapper.insert(drugList);
        // 返回
        return drugList.getId();
    }

    @Override
    public void updateDrugList(DrugListSaveReqVO updateReqVO) {
        // 校验存在
        validateDrugListExists(updateReqVO.getId());
        // 更新
        DrugListDO updateObj = BeanUtils.toBean(updateReqVO, DrugListDO.class);
        drugListMapper.updateById(updateObj);
    }

    @Override
    public void deleteDrugList(Long id) {
        // 校验存在
        validateDrugListExists(id);
        // 删除
        drugListMapper.deleteById(id);
    }

    @Override
    public void deleteDrugListListByIds(List<Long> ids) {
        // 校验存在
        validateDrugListExists(ids);
        // 删除
        drugListMapper.deleteByIds(ids);
    }

    private void validateDrugListExists(List<Long> ids) {
        List<DrugListDO> list = drugListMapper.selectByIds(ids);
        if (CollUtil.isEmpty(list) || list.size() != ids.size()) {
            throw exception(DRUG_LIST_NOT_EXISTS);
        }
    }

    private void validateDrugListExists(Long id) {
        if (drugListMapper.selectById(id) == null) {
            throw exception(DRUG_LIST_NOT_EXISTS);
        }
    }

    @Override
    public DrugListDO getDrugList(Long id) {
        return drugListMapper.selectById(id);
    }

    @Override
    public PageResult<DrugListDO> getDrugListPage(DrugListPageReqVO pageReqVO) {
        return drugListMapper.selectPage(pageReqVO);
    }

    @Override
    public List<DrugListDO> selectDrugList(DrugListPageReqVO queryVO) {
        LambdaQueryWrapper<DrugListDO> wrapper = new LambdaQueryWrapper<>();

        // 构建查询条件
        wrapper.eq(StrUtil.isNotEmpty(queryVO.getHospitalCode()),
                DrugListDO::getHospitalCode, queryVO.getHospitalCode());
        wrapper.eq(StrUtil.isNotEmpty(queryVO.getOrganizationCode()),
                DrugListDO::getOrganizationCode, queryVO.getOrganizationCode());
        wrapper.eq(StrUtil.isNotEmpty(queryVO.getYpid()),
                DrugListDO::getYpid, queryVO.getYpid());
        wrapper.eq(StrUtil.isNotEmpty(queryVO.getHosDrugId()),
                DrugListDO::getHosDrugId, queryVO.getHosDrugId());
        wrapper.like(StrUtil.isNotEmpty(queryVO.getProductName()),
                DrugListDO::getProductName, queryVO.getProductName());
        wrapper.like(StrUtil.isNotEmpty(queryVO.getManufacturer()),
                DrugListDO::getManufacturer, queryVO.getManufacturer());
        wrapper.eq(StrUtil.isNotEmpty(queryVO.getBaseFlag()),
                DrugListDO::getBaseFlag, queryVO.getBaseFlag());
        wrapper.eq(StrUtil.isNotEmpty(queryVO.getUnityPurchaseFlag()),
                DrugListDO::getUnityPurchaseFlag, queryVO.getUnityPurchaseFlag());

        wrapper.orderByDesc(DrugListDO::getCreateTime);

        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importDrugList(MultipartFile file, boolean updateSupport) throws Exception {
        // 生成批次号
        String batchNo = generateBatchNo();

        // 记录导入日志
        ImportLogSaveReqVO importLogSaveReqVO = new ImportLogSaveReqVO().setBatchNo(batchNo).setFileName(file.getOriginalFilename())
                .setFileType("DRUG_LIST").setTableName("gh_drug_list");
        Long logId = importLogSaveReqVO.getId();

        try {
            // 解析Excel文件
            List<DrugListDO> drugList = ExcelUtils.read(file, DrugListDO.class);

            if (CollUtil.isEmpty(drugList)) {
                throw exception(IMPORT_DATA_CANNOT_BE_EMPTY);
            }

            // 数据校验和处理
            Map<String, DrugListDO> existingDrugs = getExistingDrugsMap();
            List<DrugListDO> insertList = new ArrayList<>();
            List<DrugListDO> updateList = new ArrayList<>();
            List<String> errorMsgs = new ArrayList<>();

            for (int i = 0; i < drugList.size(); i++) {
                DrugListDO drug = drugList.get(i);

                // 基础数据校验
                String errorMsg = validator.validateDrugList(drug, i + 1);
                if (StrUtil.isNotEmpty(errorMsg)) {
                    errorMsgs.add(errorMsg);
                    continue;
                }

                // 设置默认值
                drug.setSerialNum((long) (i + 1));
                drug.setUploadDate(DateUtil.format(DateUtil.date(), "yyyyMMdd"));
                drug.setImportBatchNo(batchNo);
                drug.setImportTime(LocalDateTime.now());

                // 检查是否已存在
                String key = generateDrugKey(drug);
                if (existingDrugs.containsKey(key)) {
                    if (updateSupport) {
                        DrugListDO existDrug = existingDrugs.get(key);
                        drug.setId(existDrug.getId());
                        updateList.add(drug);
                    } else {
                        errorMsgs.add("第" + (i + 1) + "行数据已存在，院内药品编码："
                                + drug.getHosDrugId());
                    }
                } else {
                    insertList.add(drug);
                }
            }

            // 保存数据
            if (!insertList.isEmpty()) {
                saveBatch(insertList);
            }
            if (!updateList.isEmpty()) {
                updateBatchById(updateList);
            }

            // 更新导入日志
            ImportLogSaveReqVO importLog = new ImportLogSaveReqVO().setId(logId).setTotalRows(drugList.size())
                    .setSuccessRows(insertList.size() + updateList.size())
                    .setFailRows(errorMsgs.size()).setErrorMsg(StrUtil.join("\n", errorMsgs));
            importLogService.updateImportLog(importLog);

            return String.format("导入成功！总数：%d，新增：%d，更新：%d，失败：%d",
                    drugList.size(), insertList.size(), updateList.size(), errorMsgs.size());

        } catch (Exception e) {
            importLogService.updateImportLogFail(logId, e.getMessage());
            throw exception(IMPORT_FAILED, e.getMessage());
        }
    }

    @Override
    public DrugListDO selectByHosDrugId(String hosDrugId) {
        return getOne(new LambdaQueryWrapper<DrugListDO>()
                .eq(DrugListDO::getHosDrugId, hosDrugId)
                .last("limit 1"));
    }

    @Override
    public boolean checkHosDrugIdExist(String hosDrugId, Long excludeId) {
        LambdaQueryWrapper<DrugListDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DrugListDO::getHosDrugId, hosDrugId);
        if (excludeId != null) {
            wrapper.ne(DrugListDO::getId, excludeId);
        }
        return count(wrapper) > 0;
    }

    /**
     * 生成批次号
     */
    private String generateBatchNo() {
        // 生成时间戳部分
        String timestamp = DateUtil.format(DateUtil.date(), "yyyyMMddHHmmss");

        // 生成4位随机数字
        String randomStr = RandomUtil.randomNumbers(4);

        return "DRUG_" + timestamp + "_" + randomStr;
    }

    /**
     * 获取已存在的药品映射
     */
    private Map<String, DrugListDO> getExistingDrugsMap() {
        List<DrugListDO> existingList = list(new LambdaQueryWrapper<>());
        return existingList.stream()
                .collect(Collectors.toMap(this::generateDrugKey, drug -> drug));
    }

    /**
     * 生成药品唯一键
     */
    private String generateDrugKey(DrugListDO drug) {
        return drug.getDomainCode() + "_" + drug.getHospitalCode() + "_"
                + drug.getOrganizationCode() + "_" + drug.getHosDrugId();
    }

}