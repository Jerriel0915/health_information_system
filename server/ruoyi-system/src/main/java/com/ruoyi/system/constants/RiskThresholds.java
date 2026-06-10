package com.ruoyi.system.constants;

/**
 * 业务风险阈值常量 — M4 修复
 *
 * 集中管理报告生成时的"过/偏/合理"阈值，避免散落在代码各处的魔数。
 * 调优时改这里即可，不必动 ReportServiceImpl 逻辑。
 *
 * <p>命名约定：{@code <METRIC>_<LEVEL>_<DIRECTION>}
 * <ul>
 *   <li>METRIC: BED_USAGE / INSURANCE_PCT / COST_GROWTH / SERVICE_GROWTH</li>
 *   <li>LEVEL: CRIT_HI / WARN_HI / WARN_LO / CRIT_LO / OTHER</li>
 *   <li>DIRECTION: PCT 百分比 / RATIO 比率</li>
 * </ul>
 */
public final class RiskThresholds
{
    private RiskThresholds() {}

    // ====== 床位使用率（%）======
    public static final double BED_USAGE_CRIT_HI = 95.0;  // 严重偏高
    public static final double BED_USAGE_WARN_HI = 85.0;  // 偏高
    public static final double BED_USAGE_WARN_LO = 70.0;  // 偏低
    public static final double BED_USAGE_CRIT_LO = 60.0;  // 严重偏低

    // ====== 医保基金支出占比（%）======
    public static final double INSURANCE_PCT_WARN = 70.0;  // 医保占比过高

    // ====== 医疗总费用同比涨幅（%）======
    public static final double COST_GROWTH_WARN = 15.0;  // 涨幅过快

    // ====== 服务量同比变化（%）======
    public static final double SERVICE_GROWTH_CRIT_HI = 20.0;  // 大幅增长
    public static final double SERVICE_GROWTH_CRIT_LO = -10.0; // 大幅下降
}
