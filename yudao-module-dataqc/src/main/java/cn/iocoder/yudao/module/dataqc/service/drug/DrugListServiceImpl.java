package cn.iocoder.yudao.module.dataqc.service.drug;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.DrugListPageReqVO;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.DrugListRespVO;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.DrugListSaveReqVO;
import cn.iocoder.yudao.module.dataqc.controller.admin.importlog.vo.ImportLogSaveReqVO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.drug.DrugListDO;
import cn.iocoder.yudao.module.dataqc.dal.mysql.drug.DrugListMapper;
import cn.iocoder.yudao.module.dataqc.service.importlog.ImportLogService;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
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
import static com.baomidou.mybatisplus.extension.toolkit.Db.saveBatch;
import static com.baomidou.mybatisplus.extension.toolkit.Db.updateBatchById;

/**
 * 药品目录 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
@Slf4j
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

        return drugListMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importDrugList(MultipartFile file, boolean updateSupport) throws Exception {
        // 生成批次号
        String batchNo = generateBatchNo();

        // 记录导入日志 - 修复状态设置问题
        ImportLogSaveReqVO importLogSaveReqVO = new ImportLogSaveReqVO()
                .setBatchNo(batchNo)
                .setFileName(file.getOriginalFilename())
                .setFileType("DRUG_LIST")
                .setTableName("gh_drug_list")
                .setStatus("PROCESSING"); // 明确设置状态

        Long logId = importLogService.createImportLog(importLogSaveReqVO);

        try {
            // 解析Excel文件 - 使用安全的读取方式
            List<DrugListRespVO> drugList = null;
            try {
                drugList = ExcelUtils.read(file, DrugListRespVO.class, 3);
            } catch (Exception e) {
                log.error("Excel读取失败，尝试安全模式读取", e);
                // 如果标准读取失败，尝试逐行读取
//                drugList = safeReadExcel(file, DrugListDO.class);
            }

            if (CollUtil.isEmpty(drugList)) {
                throw exception(IMPORT_DATA_CANNOT_BE_EMPTY);
            }

            // 数据校验和处理
            Map<String, DrugListDO> existingDrugs = getExistingDrugsMap();
            List<DrugListDO> insertList = new ArrayList<>();
            List<DrugListDO> updateList = new ArrayList<>();
            List<String> errorMsgs = new ArrayList<>();

            for (int i = 0; i < drugList.size(); i++) {
                DrugListRespVO sourceDrug = drugList.get(i);
                DrugListDO drug = BeanUtil.copyProperties(sourceDrug, DrugListDO.class);

//                // 基础数据校验
//                String errorMsg = validator.validateDrugList(drug, i + 1);
//                if (StrUtil.isNotEmpty(errorMsg)) {
//                    errorMsgs.add(errorMsg);
//                    continue;
//                }

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
            ImportLogSaveReqVO importLog = new ImportLogSaveReqVO()
                    .setId(logId)
                    .setTotalRows(drugList.size())
                    .setSuccessRows(insertList.size() + updateList.size())
                    .setFailRows(errorMsgs.size())
                    .setStatus(errorMsgs.isEmpty() ? "SUCCESS" : "PARTIAL_SUCCESS")
                    .setErrorMsg(StrUtil.join("\n", errorMsgs));
            importLogService.updateImportLog(importLog);

            return String.format("导入成功！总数：%d，新增：%d，更新：%d，失败：%d",
                    drugList.size(), insertList.size(), updateList.size(), errorMsgs.size());

        } catch (Exception e) {
            log.error("导入药品目录失败", e);
            importLogService.updateImportLogFail(logId, e.getMessage());
            throw exception(IMPORT_FAILED, e.getMessage());
        }
    }
    /**
     * 安全模式读取Excel - 容错处理
     */
    private <T> List<T> safeReadExcel(MultipartFile file, Class<T> clazz) {
        List<T> result = new ArrayList<>();
        try {
            // 使用EasyExcel的监听器模式，逐行处理
            EasyExcel.read(file.getInputStream(), clazz, new AnalysisEventListener<T>() {
                @Override
                public void invoke(T data, AnalysisContext analysisContext) {
                    result.add(data);
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext analysisContext) {
                    log.info("安全模式读取完成，共{}行数据", result.size());
                }

                @Override
                public void onException(Exception exception, AnalysisContext context) throws Exception {
                    log.warn("第{}行读取异常，跳过：{}", context.readRowHolder().getRowIndex(), exception.getMessage());
                    // 不抛出异常，继续处理下一行
                }
            }).sheet().doRead();

        } catch (Exception e) {
            log.error("安全模式读取也失败", e);
            throw new RuntimeException("Excel文件读取失败", e);
        }
        return result;
    }

    @Override
    public DrugListDO selectByHosDrugId(String hosDrugId) {
        return drugListMapper.selectOne(new LambdaQueryWrapper<DrugListDO>()
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
        return drugListMapper.selectCount(wrapper) > 0;
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
        List<DrugListDO> existingList = drugListMapper.selectList();
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