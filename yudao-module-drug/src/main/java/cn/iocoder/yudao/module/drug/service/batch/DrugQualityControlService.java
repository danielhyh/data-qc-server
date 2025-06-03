// ==================== 质控服务 ====================
package cn.iocoder.yudao.module.drug.service.batch;

import cn.iocoder.yudao.module.drug.controller.admin.batch.vo.QualityControlResult;
import cn.iocoder.yudao.module.drug.enums.TableTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * 药品质控服务
 * 设计理念：专注于质控规则执行和结果分析，支持规则引擎和质量评估
 */
@Service
@Slf4j
public class DrugQualityControlService {

    /**
     * 执行表级质控检查
     * 根据表类型执行对应的质控规则，生成详细的质控报告
     */
    public QualityControlResult executeTableQualityControl(Long taskId, TableTypeEnum tableType) {
        LocalDateTime startTime = LocalDateTime.now();

        try {
            log.info("开始执行表级质控: taskId={}, tableType={}", taskId, tableType);

            // 模拟质控检查逻辑
            Random random = new Random();
            int totalRules = 5 + random.nextInt(10);
            int passedRules = totalRules - random.nextInt(3);
            int failedRules = totalRules - passedRules;

            long totalRecords = 1000 + random.nextInt(9000);
            long failedCount = random.nextInt((int) (totalRecords * 0.1));
            long passedCount = totalRecords - failedCount;
            long warningCount = random.nextInt((int) (totalRecords * 0.05));

            // 计算质量得分
            double score = ((double) passedCount / totalRecords) * 100;
            String grade = calculateGrade(score);

            LocalDateTime endTime = LocalDateTime.now();

            return QualityControlResult.builder()
                    .success(failedRules == 0)
                    .overallGrade(grade)
                    .overallScore(score)
                    .qcType("TABLE")
                    .scope(tableType.name())
                    .totalRules(totalRules)
                    .passedRules(passedRules)
                    .failedRules(failedRules)
                    .totalRecords(totalRecords)
                    .passedCount(passedCount)
                    .failedCount(failedCount)
                    .warningCount(warningCount)
                    .startTime(startTime)
                    .endTime(endTime)
                    .ruleResults(generateMockRuleResults(totalRules, passedRules))
                    .statisticSummary(new HashMap<>())
                    .build();

        } catch (Exception e) {
            log.error("质控检查异常: taskId={}, tableType={}", taskId, tableType, e);
            return QualityControlResult.builder()
                    .success(false)
                    .overallGrade("F")
                    .overallScore(0.0)
                    .qcType("TABLE")
                    .scope(tableType.name())
                    .startTime(startTime)
                    .endTime(LocalDateTime.now())
                    .ruleResults(new ArrayList<>())
                    .statisticSummary(new HashMap<>())
                    .build();
        }
    }

    /**
     * 执行整体质控检查
     */
    public QualityControlResult executeOverallQualityControl(Long taskId) {
        LocalDateTime startTime = LocalDateTime.now();

        try {
            log.info("开始执行整体质控: taskId={}", taskId);

            // 模拟整体质控逻辑
            Random random = new Random();
            double overallScore = 70 + random.nextDouble() * 30; // 70-100分
            String grade = calculateGrade(overallScore);

            return QualityControlResult.builder()
                    .success(overallScore >= 80)
                    .overallGrade(grade)
                    .overallScore(overallScore)
                    .qcType("OVERALL")
                    .scope("ALL_TABLES")
                    .startTime(startTime)
                    .endTime(LocalDateTime.now())
                    .ruleResults(new ArrayList<>())
                    .statisticSummary(new HashMap<>())
                    .build();

        } catch (Exception e) {
            log.error("整体质控检查异常: taskId={}", taskId, e);
            return QualityControlResult.builder()
                    .success(false)
                    .overallGrade("F")
                    .overallScore(0.0)
                    .qcType("OVERALL")
                    .scope("ALL_TABLES")
                    .startTime(startTime)
                    .endTime(LocalDateTime.now())
                    .ruleResults(new ArrayList<>())
                    .statisticSummary(new HashMap<>())
                    .build();
        }
    }

    private String calculateGrade(double score) {
        if (score >= 95) return "A+";
        if (score >= 90) return "A";
        if (score >= 85) return "B+";
        if (score >= 80) return "B";
        if (score >= 75) return "C+";
        if (score >= 70) return "C";
        if (score >= 60) return "D";
        return "F";
    }

    private java.util.List<QualityControlResult.QualityControlDetail> generateMockRuleResults(
            int totalRules, int passedRules) {
        java.util.List<QualityControlResult.QualityControlDetail> results = new ArrayList<>();

        for (int i = 1; i <= totalRules; i++) {
            boolean passed = i <= passedRules;
            results.add(QualityControlResult.QualityControlDetail.builder()
                    .ruleCode("QC_RULE_" + String.format("%03d", i))
                    .ruleName("质控规则" + i)
                    .ruleType("DATA_VALIDATION")
                    .passed(passed)
                    .affectedRecords(passed ? 0 : new Random().nextInt(100))
                    .resultMessage(passed ? "检查通过" : "发现数据异常")
                    .suggestion(passed ? "" : "建议检查数据格式")
                    .build());
        }

        return results;
    }
}