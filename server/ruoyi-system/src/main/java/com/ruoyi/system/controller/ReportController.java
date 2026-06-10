package com.ruoyi.system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.service.IReportService;

/**
 * 统计报告 Controller — 为 TTS 提供结构化朗读内容
 *
 * /system/report/tts-content?type=bed   返回朗读文本
 * /system/report/tts-content?type=bed&format=full  返回结构化对象
 */
@RestController
@RequestMapping("/system/report")
public class ReportController extends BaseController
{
    @Autowired
    private IReportService reportService;

    @PreAuthorize("@ss.hasPermi('system:bed:list')")
    @GetMapping("/tts-content")
    public AjaxResult ttsContent(@RequestParam(name = "type", defaultValue = "dashboard") String type,
                                 @RequestParam(name = "format", defaultValue = "text") String format)
    {
        if ("full".equalsIgnoreCase(format))
        {
            return success(reportService.generateReport(type));
        }
        return success(reportService.generateText(type));
    }

    @PreAuthorize("@ss.hasPermi('system:bed:list')")
    @GetMapping("/types")
    public AjaxResult types()
    {
        String[] types = { "bed", "cost", "service", "staff", "institution", "dashboard" };
        return success(types);
    }
}
