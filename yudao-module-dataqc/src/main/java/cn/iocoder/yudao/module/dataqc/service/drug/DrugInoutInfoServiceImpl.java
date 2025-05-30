package cn.iocoder.yudao.module.dataqc.service.drug;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.*;
import cn.iocoder.yudao.module.dataqc.controller.admin.importlog.vo.ImportLogSaveReqVO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.drug.DrugInoutInfoDO;
import cn.iocoder.yudao.module.dataqc.dal.mysql.drug.DrugInoutInfoMapper;
import cn.iocoder.yudao.module.dataqc.service.importlog.ImportLogService;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

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
@Slf4j
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

        // 记录导入日志
        ImportLogSaveReqVO importLogSaveReqVO = new ImportLogSaveReqVO()
                .setBatchNo(batchNo)
                .setFileName(file.getOriginalFilename())
                .setFileType("DRUG_INOUT_INFO")
                .setTableName("gh_drug_inout_info")
                .setStatus("PROCESSING");

        Long logId = importLogService.createImportLog(importLogSaveReqVO);

        try {
            // 根据类型读取不同的Excel结构
            List<DrugInoutInfoDO> insertList = new ArrayList<>();
            List<String> errorMsgs = new ArrayList<>();
            int totalRows = 0;

            if ("IN".equals(ioType)) {
                // 入库数据处理
                List<DrugInInfoRespVO> inDataList = ExcelUtils.read(file, DrugInInfoRespVO.class, 3);
                totalRows = inDataList.size();

                for (int i = 0; i < inDataList.size(); i++) {
                    DrugInInfoRespVO sourceInout = inDataList.get(i);
                    DrugInoutInfoDO inout = convertInDataToDO(sourceInout, ioType, sourceType, i + 1);

//                    String errorMsg = processAndValidateData(inout, i + 1);
//                    if (StrUtil.isNotEmpty(errorMsg)) {
//                        errorMsgs.add(errorMsg);
//                        continue;
//                    }

                    insertList.add(inout);
                }
            } else if ("OUT".equals(ioType)) {
                // 出库数据处理
                List<DrugOutInfoRespVO> outDataList = ExcelUtils.read(file, DrugOutInfoRespVO.class, 3);
                totalRows = outDataList.size();

                for (int i = 0; i < outDataList.size(); i++) {
                    DrugOutInfoRespVO sourceInout = outDataList.get(i);
                    DrugInoutInfoDO inout = convertOutDataToDO(sourceInout, ioType, sourceType, i + 1);

//                    String errorMsg = processAndValidateData(inout, i + 1);
//                    if (StrUtil.isNotEmpty(errorMsg)) {
//                        errorMsgs.add(errorMsg);
//                        continue;
//                    }

                    insertList.add(inout);
                }
            } else {
                throw new IllegalArgumentException("不支持的出入库类型: " + ioType);
            }

            // 保存数据
            if (!insertList.isEmpty()) {
                saveBatch(insertList);
            }

            // 更新导入日志
            ImportLogSaveReqVO updateImportLogVO = new ImportLogSaveReqVO()
                    .setId(logId)
                    .setTotalRows(totalRows)
                    .setSuccessRows(totalRows)
                    .setStatus("SUCCESS");
            importLogService.updateImportLog(updateImportLogVO);

            return String.format("导入成功！总数：%d，成功：%d，失败：%d",
                    totalRows, insertList.size(), 0);

        } catch (Exception e) {
            log.error("导入出入库数据失败", e);
            importLogService.updateImportLogFail(logId, e.getMessage());
            throw exception(IMPORT_FAILED, e.getMessage());
        }
    }

    /**
     * 转换入库数据到DO对象
     */
    private DrugInoutInfoDO convertInDataToDO(DrugInInfoRespVO sourceInout, String ioType,
                                              String sourceType, int rowNum) {
        DrugInoutInfoDO inout = new DrugInoutInfoDO();

        // 复制公共字段
        BeanUtil.copyProperties(sourceInout, inout);

        // 设置出入库类型
        inout.setIoType(ioType);
        inout.setSourceType(sourceType);

        // 处理入库特有字段
        inout.setInPackQuantity(sourceInout.getInPackQuantity());
        inout.setInDosageQuantity(sourceInout.getInDosageQuantity());
        inout.setInTotalPrice(sourceInout.getInTotalPrice());

        // 设置默认值
        inout.setSerialNum((long) rowNum);
        inout.setUploadDate(DateFormatUtils.format(new Date(), "yyyyMMdd"));
        inout.setImportTime(LocalDateTime.now());

        return inout;
    }

    /**
     * 转换出库数据到DO对象
     */
    private DrugInoutInfoDO convertOutDataToDO(DrugOutInfoRespVO sourceInout, String ioType,
                                               String sourceType, int rowNum) {
        DrugInoutInfoDO inout = new DrugInoutInfoDO();

        // 复制公共字段
        BeanUtil.copyProperties(sourceInout, inout);

        // 设置出入库类型
        inout.setIoType(ioType);
        inout.setSourceType(sourceType);

        // 处理出库特有字段
        inout.setOutPackQuantity(sourceInout.getOutPackQuantity());
        inout.setOutDosageQuantity(sourceInout.getOutDosageQuantity());

        // 设置默认值
        inout.setSerialNum((long) rowNum);
        inout.setUploadDate(DateFormatUtils.format(new Date(), "yyyyMMdd"));
        inout.setImportTime(LocalDateTime.now());

        return inout;
    }

    /**
     * 安全读取Excel数据
     */
    private List<DrugInoutInfoDO> safeReadExcelData(MultipartFile file) {
        List<DrugInoutInfoDO> result = new ArrayList<>();
        try {
            EasyExcel.read(file.getInputStream(), DrugInoutInfoDO.class, new AnalysisEventListener<DrugInoutInfoDO>() {
                @Override
                public void invoke(DrugInoutInfoDO data, AnalysisContext context) {
                    result.add(data);
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    log.info("安全模式读取出入库数据完成，共{}行", result.size());
                }

                @Override
                public void onException(Exception exception, AnalysisContext context) {
                    log.warn("第{}行读取异常，跳过：{}", context.readRowHolder().getRowIndex(), exception.getMessage());
                }
            }).sheet().doRead();

        } catch (Exception e) {
            log.error("安全模式读取出入库数据失败", e);
            throw new RuntimeException("Excel文件读取失败", e);
        }
        return result;
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