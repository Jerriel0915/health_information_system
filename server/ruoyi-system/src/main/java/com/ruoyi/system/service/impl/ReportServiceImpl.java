package com.ruoyi.system.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.MedicalBedMapper;
import com.ruoyi.system.mapper.MedicalCostMapper;
import com.ruoyi.system.mapper.MedicalServiceMapper;
import com.ruoyi.system.mapper.MedicalStaffMapper;
import com.ruoyi.system.mapper.MedicalInstitutionMapper;
import com.ruoyi.system.service.IReportService;
import com.ruoyi.system.constants.RiskThresholds;

/**
 * 统计报告 Service 实现 — 拼装 TTS 朗读文本
 *
 * 通过注入各业务模块的 Mapper，调用已存在的 summary/trend/distribution 接口，
 * 把数字组装成易朗读的中文段落。
 */
@Service
public class ReportServiceImpl implements IReportService
{
    private static final Logger log = LoggerFactory.getLogger(ReportServiceImpl.class);
    private static final DateTimeFormatter DS_FMT = DateTimeFormatter.ofPattern("yyyy 年 M 月 d 日");

    @Autowired private MedicalBedMapper bedMapper;
    @Autowired private MedicalCostMapper costMapper;
    @Autowired private MedicalServiceMapper serviceMapper;
    @Autowired private MedicalStaffMapper staffMapper;
    @Autowired private MedicalInstitutionMapper institutionMapper;

    @Override
    public String generateText(String reportType)
    {
        if (reportType == null || reportType.isEmpty())
        {
            return "未指定报告类型，无法生成内容。";
        }
        try
        {
            switch (reportType.toLowerCase())
            {
                case "bed":         return buildBedReport();
                case "cost":        return buildCostReport();
                case "service":     return buildServiceReport();
                case "staff":       return buildStaffReport();
                case "institution": return buildInstitutionReport();
                case "dashboard":   return buildDashboardReport();
                default:            return "不支持的报告类型：" + reportType;
            }
        }
        catch (Exception e)
        {
            log.error("生成报告失败 type={}", reportType, e);
            return "报告生成失败，请稍后重试。";
        }
    }

    @Override
    public Map<String, Object> generateReport(String reportType)
    {
        Map<String, Object> result = new HashMap<>();
        result.put("type", reportType);
        result.put("title", titleOf(reportType));
        result.put("generatedAt", LocalDate.now().format(DS_FMT));
        result.put("text", generateText(reportType));
        return result;
    }

    // ====== 各类型报告 ======

    private String buildBedReport()
    {
        Map<String, Object> summary = bedMapper.selectBedSummary(null, null);
        List<Map<String, Object>> trend = bedMapper.selectBedTrend();
        List<Map<String, Object>> usage = bedMapper.selectBedUsageRate(null, null);

        long totalBed = toLong(summary.get("totalBedCount"));
        long actual = toLong(summary.get("totalActualBedCount"));
        long used = toLong(summary.get("totalUsedBedCount"));
        double usageRate = toDouble(summary.get("avgUsageRate"));

        StringBuilder sb = new StringBuilder();
        sb.append("【").append(LocalDate.now().format(DS_FMT)).append(" 床位统计报告】\n");
        sb.append("全市医疗卫生机构床位总数 ").append(totalBed).append(" 张，").append("实有床位 ").append(actual).append(" 张。\n");
        sb.append("当前床位使用率为 ").append(String.format("%.1f", usageRate)).append("%。\n");

        if (trend != null && trend.size() >= 2)
        {
            Map<String, Object> last = trend.get(trend.size() - 1);
            Map<String, Object> prev = trend.get(trend.size() - 2);
            long lastBed = toLong(last.get("bedCount"));
            long prevBed = toLong(prev.get("bedCount"));
            if (prevBed > 0)
            {
                double growth = (lastBed - prevBed) * 100.0 / prevBed;
                sb.append("对比上年，床位总数").append(growth >= 0 ? "增长" : "下降")
                  .append(String.format("%.1f", Math.abs(growth))).append("%。\n");
            }
        }

        if (usage != null && !usage.isEmpty())
        {
            Map<String, Object> top = usage.get(0);
            sb.append("使用率最高的科室是 ").append(safe(top.get("name"))).append("，达到 ")
              .append(String.format("%.1f", toDouble(top.get("usageRate")))).append("%。\n");
        }

        // Phase 5: 风险预警 + 建议（M4: 阈值走常量）
        appendRiskWarning(sb, "床位使用率", usageRate,
                RiskThresholds.BED_USAGE_CRIT_HI, RiskThresholds.BED_USAGE_WARN_HI,
                RiskThresholds.BED_USAGE_WARN_LO, RiskThresholds.BED_USAGE_CRIT_LO);
        sb.append("建议：");
        if (usageRate >= 85)
            sb.append("床位紧张，应优先增加基层医疗和康复护理床位供给。");
        else if (usageRate >= 60)
            sb.append("床位利用率处于合理区间，建议继续优化床位资源配置。");
        else
            sb.append("床位利用率偏低，应加强分级诊疗和床位周转效率。");
        return sb.toString();
    }

    private String buildCostReport()
    {
        Map<String, Object> summary = costMapper.selectCostSummary(null, null);
        Map<String, Object> composition = costMapper.selectCostComposition(null, null);
        List<Map<String, Object>> trend = costMapper.selectCostTrend();

        double total = toDouble(summary.get("totalCost"));
        double avg = toDouble(summary.get("avgCost"));
        double insPaid = toDouble(summary.get("totalInsurancePaid"));
        double selfPaid = toDouble(summary.get("totalSelfPaid"));

        StringBuilder sb = new StringBuilder();
        sb.append("【").append(LocalDate.now().format(DS_FMT)).append(" 医疗费用统计报告】\n");
        sb.append("本期医疗费用总额 ").append(formatWan(total)).append("，").append("次均费用 ")
          .append(String.format("%.0f", avg)).append(" 元。\n");
        if (total > 0)
        {
            double insPct = insPaid * 100.0 / total;
            sb.append("医保基金支付 ").append(formatWan(insPaid)).append("，占比 ")
              .append(String.format("%.1f", insPct)).append("%；个人自付 ")
              .append(formatWan(selfPaid)).append("，占比 ")
              .append(String.format("%.1f", 100 - insPct)).append("%。\n");
        }

        if (composition != null)
        {
            String maxKey = maxCostCategory(composition);
            if (maxKey != null)
            {
                double maxVal = toDouble(composition.get(maxKey));
                sb.append("费用构成中，").append(categoryLabel(maxKey)).append(" 最高，金额 ")
                  .append(formatWan(maxVal)).append("。\n");
            }
        }

        if (trend != null && trend.size() >= 2)
        {
            Map<String, Object> last = trend.get(trend.size() - 1);
            Map<String, Object> prev = trend.get(trend.size() - 2);
            double lastT = toDouble(last.get("totalCost"));
            double prevT = toDouble(prev.get("totalCost"));
            if (prevT > 0)
            {
                double delta = (lastT - prevT) * 100.0 / prevT;
                sb.append("同比上年，医疗总费用").append(delta >= 0 ? "上升" : "下降")
                  .append(String.format("%.1f", Math.abs(delta))).append("%。\n");
                // Phase 5: 费用同比异常增长预警（M4: 阈值走常量）
                if (delta > RiskThresholds.COST_GROWTH_WARN)
                {
                    sb.append("【风险提示】医疗总费用同比涨幅超过 ")
                      .append(String.format("%.1f", RiskThresholds.COST_GROWTH_WARN))
                      .append("%，建议核查异常收费情况。\n");
                }
            }
        }

        // Phase 5: 医保占比风险预警（M4: 阈值走常量）
        if (total > 0)
        {
            double insPct = insPaid * 100.0 / total;
            if (insPct > RiskThresholds.INSURANCE_PCT_WARN)
                sb.append("【风险提示】医保基金支出占比 ").append(String.format("%.1f", insPct))
                  .append("%，处于较高水平。\n");
        }

        sb.append("建议：");
        if (total > 0 && (insPaid / total) > 0.7)
            sb.append("医保基金支出占比较高，需关注药品与检查费用增长。");
        else
            sb.append("继续优化费用结构，控制不合理增长。");
        return sb.toString();
    }

    private String buildServiceReport()
    {
        Map<String, Object> summary = serviceMapper.selectServiceSummary(null, null);
        List<Map<String, Object>> trend = serviceMapper.selectServiceTrend();
        List<Map<String, Object>> typeDist = serviceMapper.selectServiceTypeDistribution(null, null);

        long total = toLong(summary.get("totalServices"));
        long outpatient = toLong(summary.get("outpatientCount"));
        long inpatient = toLong(summary.get("inpatientCount"));
        Object avgDays = summary.get("avgDaysInHospital");

        StringBuilder sb = new StringBuilder();
        sb.append("【").append(LocalDate.now().format(DS_FMT)).append(" 医疗服务量报告】\n");
        sb.append("本期医疗服务总量 ").append(total).append(" 人次，").append("其中门诊 ")
          .append(outpatient).append(" 人次，住院 ").append(inpatient).append(" 人次。\n");
        if (total > 0)
        {
            double outPct = outpatient * 100.0 / total;
            sb.append("门诊占比 ").append(String.format("%.1f", outPct)).append("%。\n");
        }
        if (avgDays != null)
        {
            sb.append("平均住院天数 ").append(avgDays).append(" 天。\n");
        }
        if (typeDist != null && !typeDist.isEmpty())
        {
            Map<String, Object> top = typeDist.get(0);
            sb.append("服务量最高的类型是 ").append(safe(top.get("name"))).append("，共 ")
              .append(toLong(top.get("value"))).append(" 人次。\n");
        }
        if (trend != null && trend.size() >= 2)
        {
            Map<String, Object> last = trend.get(trend.size() - 1);
            Map<String, Object> prev = trend.get(trend.size() - 2);
            long lastV = toLong(last.get("value"));
            long prevV = toLong(prev.get("value"));
            if (prevV > 0)
            {
                double growth = (lastV - prevV) * 100.0 / prevV;
                sb.append("同比上年，服务量").append(growth >= 0 ? "增长" : "下降")
                  .append(String.format("%.1f", Math.abs(growth))).append("%。\n");
                // Phase 5: 服务量异常波动预警（M4: 阈值走常量）
                if (growth > RiskThresholds.SERVICE_GROWTH_CRIT_HI)
                    sb.append("【风险提示】服务量同比增长超过 ")
                      .append(String.format("%.1f", RiskThresholds.SERVICE_GROWTH_CRIT_HI))
                      .append("%，需关注医院承载能力。\n");
                else if (growth < RiskThresholds.SERVICE_GROWTH_CRIT_LO)
                    sb.append("【风险提示】服务量同比下降超过 ")
                      .append(String.format("%.1f", Math.abs(RiskThresholds.SERVICE_GROWTH_CRIT_LO)))
                      .append("%，需分析原因。\n");
            }
        }
        sb.append("建议：持续推进分级诊疗，加强基层首诊能力建设。");
        return sb.toString();
    }

    private String buildStaffReport()
    {
        Map<String, Object> summary = staffMapper.selectStaffSummary(null);
        long total = toLong(summary.get("totalStaff"));
        long active = toLong(summary.get("activeCount"));
        long titleCount = toLong(summary.get("titleCount"));
        long deptCount = toLong(summary.get("deptCount"));
        StringBuilder sb = new StringBuilder();
        sb.append("【").append(LocalDate.now().format(DS_FMT)).append(" 医务人员统计报告】\n");
        sb.append("全市医务人员总数 ").append(total).append(" 人，");
        if (total > 0) sb.append("在岗 ").append(active).append(" 人。\n");
        else           sb.append("\n");
        sb.append("覆盖 ").append(titleCount).append(" 个职称类别，分布在 ").append(deptCount).append(" 个科室。\n");
        sb.append("建议：持续优化医务人员结构，加强基层医疗人才引进。");
        return sb.toString();
    }

    private String buildInstitutionReport()
    {
        Map<String, Object> summary = institutionMapper.selectInstitutionSummary(null);
        long total = toLong(summary.get("totalInstitutions"));
        long active = toLong(summary.get("activeCount"));
        long typeCount = toLong(summary.get("typeCount"));
        long levelCount = toLong(summary.get("levelCount"));
        StringBuilder sb = new StringBuilder();
        sb.append("【").append(LocalDate.now().format(DS_FMT)).append(" 医疗机构统计报告】\n");
        sb.append("全市医疗卫生机构总数 ").append(total).append(" 家，");
        if (total > 0) sb.append("在营 ").append(active).append(" 家。\n");
        else           sb.append("\n");
        sb.append("机构类型 ").append(typeCount).append(" 种，机构等级 ").append(levelCount).append(" 级。\n");
        sb.append("建议：完善分级诊疗体系，提升基层服务能力。");
        return sb.toString();
    }

    private String buildDashboardReport()
    {
        // L3 修复：每个子报告单独 try-catch，避免一个失败导致整个 dashboard 崩溃
        String bed = safeSubReport(() -> truncate(buildBedReport(), 200));
        String cost = safeSubReport(() -> truncate(buildCostReport(), 200));
        String service = safeSubReport(() -> truncate(buildServiceReport(), 200));
        return "【" + LocalDate.now().format(DS_FMT) + " 系统总览】\n"
             + "床位：" + bed + "\n"
             + "费用：" + cost + "\n"
             + "服务：" + service;
    }

    /**
     * L3 修复：子报告生成的安全包装器
     */
    private String safeSubReport(java.util.function.Supplier<String> builder)
    {
        try
        {
            return builder.get();
        }
        catch (Exception e)
        {
            log.warn("子报告生成失败", e);
            return "（子报告生成失败）";
        }
    }

    // ====== 辅助方法 ======

    private static String titleOf(String type)
    {
        if (type == null) return "统计报告";
        switch (type.toLowerCase())
        {
            case "bed":         return "床位统计报告";
            case "cost":        return "费用统计报告";
            case "service":     return "服务量报告";
            case "staff":       return "医务人员报告";
            case "institution": return "医疗机构报告";
            case "dashboard":   return "系统总览报告";
            default:            return "统计报告";
        }
    }

    private static long toLong(Object o)
    {
        if (o == null) return 0L;
        if (o instanceof Number) return ((Number) o).longValue();
        try { return Long.parseLong(o.toString()); } catch (Exception e) { return 0L; }
    }

    private static double toDouble(Object o)
    {
        if (o == null) return 0d;
        if (o instanceof Number) return ((Number) o).doubleValue();
        try { return Double.parseDouble(o.toString()); } catch (Exception e) { return 0d; }
    }

    private static String safe(Object o) { return o == null ? "未知" : o.toString(); }

    /** 把数字格式化为"X.X 万元" / "X.X 亿元" */
    private static String formatWan(double amount)
    {
        if (amount >= 1_0000_0000) return String.format("%.2f 亿元", amount / 1_0000_0000);
        if (amount >= 1_0000)      return String.format("%.2f 万元", amount / 1_0000);
        return String.format("%.0f 元", amount);
    }

    private static String truncate(String s, int max)
    {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "……";
    }

    private static String maxCostCategory(Map<String, Object> composition)
    {
        String[] keys = {"drugCost", "treatmentCost", "surgeryCost", "inspectionCost",
                         "laboratoryCost", "bedCost", "nursingCost", "otherCost"};
        String best = null;
        double bestVal = -1;
        for (String k : keys)
        {
            double v = toDouble(composition.get(k));
            if (v > bestVal) { bestVal = v; best = k; }
        }
        return bestVal > 0 ? best : null;
    }

    private static String categoryLabel(String key)
    {
        switch (key)
        {
            case "drugCost":         return "药品费用";
            case "treatmentCost":    return "治疗费用";
            case "surgeryCost":      return "手术费用";
            case "inspectionCost":   return "检查费用";
            case "laboratoryCost":   return "化验费用";
            case "bedCost":          return "床位费用";
            case "nursingCost":      return "护理费用";
            case "otherCost":        return "其他费用";
            default:                  return key;
        }
    }

    /**
     * 通用风险预警生成器 — Phase 5
     *
     * @param sb       目标 StringBuilder
     * @param metric   指标名称（如"床位使用率"）
     * @param value    当前值
     * @param critHi   严重高线（>= 触发"严重偏高"）
     * @param warnHi   预警高线（>= 触发"偏高提醒"）
     * @param warnLo   预警低线（<= 触发"偏低提醒"）
     * @param critLo   严重低线（<= 触发"严重偏低"）
     */
    private static void appendRiskWarning(StringBuilder sb, String metric, double value,
                                          double critHi, double warnHi, double warnLo, double critLo)
    {
        if (value >= critHi)
            sb.append("【风险提示】").append(metric).append("过高（")
              .append(String.format("%.1f", value)).append("%），建议立即采取扩容或调度措施。\n");
        else if (value >= warnHi)
            sb.append("【风险提示】").append(metric).append("偏高（")
              .append(String.format("%.1f", value)).append("%），建议关注。\n");
        else if (value <= critLo)
            sb.append("【风险提示】").append(metric).append("过低（")
              .append(String.format("%.1f", value)).append("%），资源利用不充分。\n");
        else if (value <= warnLo)
            sb.append("【风险提示】").append(metric).append("偏低（")
              .append(String.format("%.1f", value)).append("%），需提升利用效率。\n");
    }
}
