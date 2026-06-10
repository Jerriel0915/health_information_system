package com.ruoyi.system.service;

import java.util.Map;

/**
 * 统计报告生成服务 — 拼装供 TTS 朗读的中文文本
 *
 * 设计目标：把"零散的统计数字" + "业务建议"组装成一段连贯的话，
 * 避免前端硬编码报告内容。
 */
public interface IReportService
{
    /**
     * 生成指定类型的报告朗读文本
     *
     * @param reportType 报告类型：bed/cost/service/staff/institution/dashboard
     * @return 朗读文本（中文），首尾空格已 trim
     */
    String generateText(String reportType);

    /**
     * 生成报告（结构化版，便于前端展示/调试）
     *
     * @param reportType 报告类型
     * @return {title, text, generatedAt, type}
     */
    Map<String, Object> generateReport(String reportType);
}
