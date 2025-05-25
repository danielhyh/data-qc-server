package cn.iocoder.yudao.module.dataqc.service.drug;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.HosResourceInfoPageReqVO;
import cn.iocoder.yudao.module.dataqc.controller.admin.drug.vo.HosResourceInfoSaveReqVO;
import cn.iocoder.yudao.module.dataqc.dal.dataobject.drug.HosResourceInfoDO;
import cn.iocoder.yudao.module.dataqc.dal.mysql.drug.HosResourceInfoMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.dataqc.enums.ErrorCodeConstants.*;
import static com.baomidou.mybatisplus.extension.toolkit.Db.saveBatch;
import static com.baomidou.mybatisplus.extension.toolkit.Db.updateBatchById;

/**
 * 医疗机构资源信息 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
@Slf4j
public class HosResourceInfoServiceImpl implements HosResourceInfoService {

    @Resource
    private HosResourceInfoMapper hosResourceInfoMapper;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importResourceData(MultipartFile file, boolean updateSupport) throws Exception {
        // 这是Excel导入的标准流程，请仔细学习这个模式

        // 第一步：生成批次号，用于追踪这次导入
        String batchNo = generateBatchNo();

        try {
            // 第二步：解析Excel文件
            List<HosResourceInfoDO> dataList = ExcelUtils.read(file, HosResourceInfoDO.class);

            if (CollUtil.isEmpty(dataList)) {
                throw exception(IMPORT_DATA_CANNOT_BE_EMPTY);
            }

            // 第三步：数据校验和处理
            List<HosResourceInfoDO> insertList = new ArrayList<>();
            List<HosResourceInfoDO> updateList = new ArrayList<>();
            List<String> errorMsgs = new ArrayList<>();

            // 获取已存在的数据，用于判断是新增还是更新
            Map<String, HosResourceInfoDO> existingDataMap = getExistingDataMap();

            for (int i = 0; i < dataList.size(); i++) {
                HosResourceInfoDO data = dataList.get(i);

                // 数据校验
                String errorMsg = validateResourceData(data, i + 1);
                if (StrUtil.isNotEmpty(errorMsg)) {
                    errorMsgs.add(errorMsg);
                    continue;
                }

                // 设置默认值
                data.setImportBatchNo(batchNo);
                data.setUploadDate(DateUtil.format(DateUtil.date(), "yyyyMMdd"));

                // 判断是否已存在
                String key = generateDataKey(data);
                if (existingDataMap.containsKey(key)) {
                    if (updateSupport) {
                        HosResourceInfoDO existData = existingDataMap.get(key);
                        data.setId(existData.getId());
                        updateList.add(data);
                    } else {
                        errorMsgs.add("第" + (i + 1) + "行数据已存在");
                    }
                } else {
                    insertList.add(data);
                }
            }

            // 第四步：批量保存数据
            if (!insertList.isEmpty()) {
                saveBatch(insertList);
            }
            if (!updateList.isEmpty()) {
                updateBatchById(updateList);
            }

            // 第五步：返回结果统计
            return String.format("导入成功！总数：%d，新增：%d，更新：%d，失败：%d",
                    dataList.size(), insertList.size(), updateList.size(), errorMsgs.size());

        } catch (Exception e) {
            log.error("导入医疗机构资源数据失败", e);
            throw exception(IMPORT_FAILED, e.getMessage());
        }
    }
    /**
     * 获取已存在数据的映射表，用于快速判断重复数据
     * 设计原理：将数据库查询结果转换为Map结构，实现O(1)时间复杂度的查找
     *
     * @return Map<业务唯一键, 数据对象> 的映射关系
     */
    private Map<String, HosResourceInfoDO> getExistingDataMap() {
        try {
            // 查询所有现有数据 - 这里可能需要根据实际业务调整查询范围
            List<HosResourceInfoDO> existingDataList = hosResourceInfoMapper.selectList(null);

            // 将List转换为Map，以业务唯一键作为key
            Map<String, HosResourceInfoDO> dataMap = new HashMap<>();

            for (HosResourceInfoDO data : existingDataList) {
                String key = generateDataKey(data);
                dataMap.put(key, data);
            }

            log.info("已加载 {} 条现有数据用于重复性检查", dataMap.size());
            return dataMap;

        } catch (Exception e) {
            log.error("获取现有数据映射失败", e);
            // 返回空Map，让导入流程继续，但所有数据都会被当作新增处理
            return new HashMap<>();
        }
    }

    /**
     * 生成数据的业务唯一键
     * 设计原理：根据业务规则组合多个字段形成唯一标识
     *
     * 业务分析：
     * - 医疗机构资源信息的唯一性通常由"医疗机构代码"确定
     * - 但也可能需要结合其他维度，如资源类型、时间等
     *
     * @param data 资源信息数据对象
     * @return 业务唯一键字符串
     */
    private String generateDataKey(HosResourceInfoDO data) {
        // 空值检查，避免NPE
        if (data == null) {
            return "";
        }

        // 方案一：仅使用医疗机构代码作为唯一键（最简单的情况）
        if (StrUtil.isNotEmpty(data.getHospitalCode())) {
            return data.getHospitalCode();
        }

        // 方案二：组合多个字段形成复合键（适用于更复杂的业务场景）
        StringBuilder keyBuilder = new StringBuilder();

        // 医疗机构代码（主键）
        keyBuilder.append(StrUtil.nullToEmpty(data.getHospitalCode())).append("|");

        // 组织机构代码（辅助键）
        keyBuilder.append(StrUtil.nullToEmpty(data.getOrganizationCode())).append("|");

        // 如果存在资源类型字段，也可以加入
        // keyBuilder.append(StrUtil.nullToEmpty(data.getResourceType())).append("|");

        // 如果需要按时间维度区分，也可以加入日期
        // keyBuilder.append(StrUtil.nullToEmpty(data.getUploadDate()));

        return keyBuilder.toString();
    }

    private String generateBatchNo() {
        return "RES_" + DateUtil.format(DateUtil.date(), "yyyyMMddHHmmss") + "_" + RandomUtil.randomNumbers(4);
    }

    private String validateResourceData(HosResourceInfoDO data, int rowNum) {
        StringBuilder errors = new StringBuilder();

        if (StrUtil.isEmpty(data.getHospitalCode())) {
            errors.append("医疗机构代码不能为空；");
        }
        if (StrUtil.isEmpty(data.getOrganizationCode())) {
            errors.append("组织机构代码不能为空；");
        }
        // ... 其他校验逻辑

        if (errors.length() > 0) {
            return "第" + rowNum + "行：" + errors.toString();
        }
        return null;
    }

    @Override
    public Long createHosResourceInfo(HosResourceInfoSaveReqVO createReqVO) {
        // 插入
        HosResourceInfoDO hosResourceInfo = BeanUtils.toBean(createReqVO, HosResourceInfoDO.class);
        hosResourceInfoMapper.insert(hosResourceInfo);
        // 返回
        return hosResourceInfo.getId();
    }

    @Override
    public void updateHosResourceInfo(HosResourceInfoSaveReqVO updateReqVO) {
        // 校验存在
        validateHosResourceInfoExists(updateReqVO.getId());
        // 更新
        HosResourceInfoDO updateObj = BeanUtils.toBean(updateReqVO, HosResourceInfoDO.class);
        hosResourceInfoMapper.updateById(updateObj);
    }

    @Override
    public void deleteHosResourceInfo(Long id) {
        // 校验存在
        validateHosResourceInfoExists(id);
        // 删除
        hosResourceInfoMapper.deleteById(id);
    }

    @Override
    public void deleteHosResourceInfoListByIds(List<Long> ids) {
        // 校验存在
        validateHosResourceInfoExists(ids);
        // 删除
        hosResourceInfoMapper.deleteByIds(ids);
    }

    private void validateHosResourceInfoExists(List<Long> ids) {
        List<HosResourceInfoDO> list = hosResourceInfoMapper.selectByIds(ids);
        if (CollUtil.isEmpty(list) || list.size() != ids.size()) {
            throw exception(HOS_RESOURCE_INFO_NOT_EXISTS);
        }
    }

    private void validateHosResourceInfoExists(Long id) {
        if (hosResourceInfoMapper.selectById(id) == null) {
            throw exception(HOS_RESOURCE_INFO_NOT_EXISTS);
        }
    }

    @Override
    public HosResourceInfoDO getHosResourceInfo(Long id) {
        return hosResourceInfoMapper.selectById(id);
    }

    @Override
    public PageResult<HosResourceInfoDO> getHosResourceInfoPage(HosResourceInfoPageReqVO pageReqVO) {
        return hosResourceInfoMapper.selectPage(pageReqVO);
    }

}