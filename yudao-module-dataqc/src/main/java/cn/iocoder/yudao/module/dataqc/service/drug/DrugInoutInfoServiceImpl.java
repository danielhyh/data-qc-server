package cn.iocoder.yudao.module.dataqc.service.drug;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.DrugInoutInfoPageReqVO;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.DrugInoutInfoSaveReqVO;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.InoutStatVO;
import cn.iocoder.yudao.module.dataqc.controller.admin.importlog.vo.ImportLogSaveReqVO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.drug.DrugInoutInfoDO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.drug.DrugListDO;
import cn.iocoder.yudao.module.dataqc.dal.mysql.drug.DrugInoutInfoMapper;
import cn.iocoder.yudao.module.dataqc.service.importlog.ImportLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.dataqc.enums.ErrorCodeConstants.DRUG_INOUT_INFO_NOT_EXISTS;
import static cn.iocoder.yudao.module.dataqc.enums.ErrorCodeConstants.IMPORT_FAILED;
import static com.baomidou.mybatisplus.extension.toolkit.Db.list;
import static com.baomidou.mybatisplus.extension.toolkit.Db.saveBatch;

/**
 * 药品出入库 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class DrugInoutInfoServiceImpl implements DrugInoutInfoService {

    @Resource
    private DrugInoutInfoMapper drugInoutInfoMapper;
    @Resource
    private DrugListService drugListService;
    @Resource
    private ImportLogService importLogService;
    @Resource
    private DrugImportValidator validator;

    @Override
    public Long createDrugInoutInfo(DrugInoutInfoSaveReqVO createReqVO) {
        // 插入
        DrugInoutInfoDO drugInoutInfo = BeanUtils.toBean(createReqVO, DrugInoutInfoDO.class);
        drugInoutInfoMapper.insert(drugInoutInfo);
        // 返回
        return drugInoutInfo.getId();
    }

    @Override
    public void updateDrugInoutInfo(DrugInoutInfoSaveReqVO updateReqVO) {
        // 校验存在
        validateDrugInoutInfoExists(updateReqVO.getId());
        // 更新
        DrugInoutInfoDO updateObj = BeanUtils.toBean(updateReqVO, DrugInoutInfoDO.class);
        drugInoutInfoMapper.updateById(updateObj);
    }

    @Override
    public void deleteDrugInoutInfo(Long id) {
        // 校验存在
        validateDrugInoutInfoExists(id);
        // 删除
        drugInoutInfoMapper.deleteById(id);
    }

    @Override
        public void deleteDrugInoutInfoListByIds(List<Long> ids) {
        // 校验存在
        validateDrugInoutInfoExists(ids);
        // 删除
        drugInoutInfoMapper.deleteByIds(ids);
        }

    private void validateDrugInoutInfoExists(List<Long> ids) {
        List<DrugInoutInfoDO> list = drugInoutInfoMapper.selectByIds(ids);
        if (CollUtil.isEmpty(list) || list.size() != ids.size()) {
            throw exception(DRUG_INOUT_INFO_NOT_EXISTS);
        }
    }

    private void validateDrugInoutInfoExists(Long id) {
        if (drugInoutInfoMapper.selectById(id) == null) {
            throw exception(DRUG_INOUT_INFO_NOT_EXISTS);
        }
    }

    @Override
    public DrugInoutInfoDO getDrugInoutInfo(Long id) {
        return drugInoutInfoMapper.selectById(id);
    }

    @Override
    public PageResult<DrugInoutInfoDO> getDrugInoutInfoPage(DrugInoutInfoPageReqVO pageReqVO) {
        return drugInoutInfoMapper.selectPage(pageReqVO);
    }
    @Override
    public List<DrugInoutInfoDO> selectInoutList(DrugInoutInfoPageReqVO queryVO) {
        LambdaQueryWrapper<DrugInoutInfoDO> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(StrUtil.isNotEmpty(queryVO.getHospitalCode()),
                DrugInoutInfoDO::getHospitalCode, queryVO.getHospitalCode());
        wrapper.eq(StrUtil.isNotEmpty(queryVO.getHosDrugId()),
                DrugInoutInfoDO::getHosDrugId, queryVO.getHosDrugId());
        wrapper.like(StrUtil.isNotEmpty(queryVO.getProductName()),
                DrugInoutInfoDO::getProductName, queryVO.getProductName());
        wrapper.eq(StrUtil.isNotEmpty(queryVO.getIoType()),
                DrugInoutInfoDO::getIoType, queryVO.getIoType());
        wrapper.like(StrUtil.isNotEmpty(queryVO.getSupplierName()),
                DrugInoutInfoDO::getSupplierName, queryVO.getSupplierName());
        wrapper.eq(StrUtil.isNotEmpty(queryVO.getBatchNo()),
                DrugInoutInfoDO::getBatchNo, queryVO.getBatchNo());

        // 日期范围查询
        if (StrUtil.isNotEmpty(queryVO.getBeginDate())
                && StrUtil.isNotEmpty(queryVO.getEndDate())) {
            wrapper.between(DrugInoutInfoDO::getOutInDate,
                    queryVO.getBeginDate(), queryVO.getEndDate());
        }

        wrapper.orderByDesc(DrugInoutInfoDO::getOutInDate);

        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importInData(MultipartFile file, boolean updateSupport) throws Exception {
        return importData(file, updateSupport, "IN", "IN_IMPORT");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importOutData(MultipartFile file, boolean updateSupport) throws Exception {
        return importData(file, updateSupport, "OUT", "OUT_IMPORT");
    }

    private String importData(MultipartFile file, boolean updateSupport,
                              String ioType, String sourceType) throws Exception {

        String batchNo = generateBatchNo();
        ImportLogSaveReqVO importLogSaveReqVO = new ImportLogSaveReqVO().setBatchNo(batchNo).setFileName(file.getOriginalFilename())
                .setFileType("DRUG_INOUT_INFO").setTableName("gh_drug_inout_info");
        Long logId = importLogService.createImportLog(importLogSaveReqVO);

        try {
            List<DrugInoutInfoDO> dataList = ExcelUtils.read(file, DrugInoutInfoDO.class);

            List<DrugInoutInfoDO> insertList = new ArrayList<>();
            List<String> errorMsgs = new ArrayList<>();

            for (int i = 0; i < dataList.size(); i++) {
                DrugInoutInfoDO inout = dataList.get(i);

                // 设置出入库类型
                inout.setIoType(ioType);
                inout.setSourceType(sourceType);

                // 数据校验
                String errorMsg = validator.validateInoutInfo(inout, i + 1);
                if (StrUtil.isNotEmpty(errorMsg)) {
                    errorMsgs.add(errorMsg);
                    continue;
                }

                // 验证药品是否存在于药品目录
                DrugListDO drug = drugListService.selectByHosDrugId(inout.getHosDrugId());
                if (drug == null) {
                    errorMsgs.add("第" + (i + 1) + "行：院内药品编码["
                            + inout.getHosDrugId() + "]在药品目录中不存在");
                    continue;
                }

                // 自动填充药品信息
                inout.setYpid(drug.getYpid());
                inout.setPrDrugId(drug.getPrDrugId());
                inout.setProductName(drug.getProductName());

                // 计算制剂单位数量
                if ("IN".equals(ioType)) {
                    inout.setInDosageQuantity(
                            inout.getInPackQuantity() * drug.getDrugFactor().longValue());
                    inout.setInDosagePrice(
                            inout.getInPackPrice().divide(drug.getDrugFactor(), 2,
                                    BigDecimal.ROUND_HALF_UP));
                } else {
                    inout.setOutDosageQuantity(
                            inout.getOutPackQuantity() * drug.getDrugFactor().longValue());
                }

                // 设置其他默认值
                inout.setSerialNum((long) (i + 1));
                inout.setUploadDate(DateFormatUtils.format(new Date(), "yyyyMMdd"));
                inout.setImportBatchNo(batchNo);
                inout.setImportTime(LocalDateTime.now());

                insertList.add(inout);
            }

            // 保存数据
            if (!insertList.isEmpty()) {
                saveBatch(insertList);
            }

            // 更新导入日志
            ImportLogSaveReqVO updateImportLogVO = new ImportLogSaveReqVO().setId(logId).setSuccessRows(insertList.size())
                    .setFailRows(errorMsgs.size()).setErrorMsg(StrUtil.join("\n", errorMsgs));
            importLogService.updateImportLog(updateImportLogVO);

            return String.format("导入成功！总数：%d，成功：%d，失败：%d",
                    dataList.size(), insertList.size(), errorMsgs.size());

        } catch (Exception e) {
            importLogService.updateImportLogFail(logId, e.getMessage());
            throw exception(IMPORT_FAILED, e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> getStockSummary(DrugInoutInfoPageReqVO queryVO) {
        return drugInoutInfoMapper.selectStockSummary(queryVO);
    }

    @Override
    public InoutStatVO getInoutStatistics(String startDate, String endDate) {
        return drugInoutInfoMapper.selectInoutStatistics(startDate, endDate);
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

}