package com.ruoyi.system.controller;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.MedicalBed;
import com.ruoyi.system.mapper.MedicalBedMapper;
import com.ruoyi.system.service.IMedicalBedService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * ???????Controller
 * 
 * @author ruoyi
 * @date 2026-05-27
 */
@RestController
@RequestMapping("/system/bed")
public class MedicalBedController extends BaseController
{
    @Autowired
    private IMedicalBedService medicalBedService;
    @Autowired
    private MedicalBedMapper medicalBedMapper;


    /**
     * 查询???????列表
     */
    @PreAuthorize("@ss.hasPermi('system:bed:list')")
    @GetMapping("/list")
    public TableDataInfo list(MedicalBed medicalBed)
    {
        startPage();
        List<MedicalBed> list = medicalBedService.selectMedicalBedList(medicalBed);
        return getDataTable(list);
    }

    /**
     * 导出???????列表
     */
    @PreAuthorize("@ss.hasPermi('system:bed:export')")
    @Log(title = "???????", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MedicalBed medicalBed)
    {
        List<MedicalBed> list = medicalBedService.selectMedicalBedList(medicalBed);
        ExcelUtil<MedicalBed> util = new ExcelUtil<MedicalBed>(MedicalBed.class);
        util.exportExcel(response, list, "???????数据");
    }

    /**
     * 获取???????详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:bed:query')")
    @GetMapping("/detail/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(medicalBedService.selectMedicalBedById(id));
    }

    /**
     * 新增???????
     */
    @PreAuthorize("@ss.hasPermi('system:bed:add')")
    @Log(title = "???????", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MedicalBed medicalBed)
    {
        return toAjax(medicalBedService.insertMedicalBed(medicalBed));
    }

    /**
     * 修改???????
     */
    @PreAuthorize("@ss.hasPermi('system:bed:edit')")
    @Log(title = "???????", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MedicalBed medicalBed)
    {
        return toAjax(medicalBedService.updateMedicalBed(medicalBed));
    }

    /**
     * 删除???????
     */
    @PreAuthorize("@ss.hasPermi('system:bed:remove')")
    @Log(title = "???????", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(medicalBedService.deleteMedicalBedByIds(ids));
    }
    // ====== 统计分析接口 ======

    @PreAuthorize("@ss.hasPermi('system:bed:list')")
    @GetMapping("/summary")
    public AjaxResult summary(Long orgId, Integer year) {
        return success(medicalBedMapper.selectBedSummary(orgId, year));
    }

    @PreAuthorize("@ss.hasPermi('system:bed:list')")
    @GetMapping("/dept-distribution")
    public AjaxResult deptDistribution(Long orgId, Integer year) {
        return success(medicalBedMapper.selectBedDeptDistribution(orgId, year));
    }

    @PreAuthorize("@ss.hasPermi('system:bed:list')")
    @GetMapping("/region-distribution")
    public AjaxResult regionDistribution() {
        return success(medicalBedMapper.selectBedRegionDistribution());
    }

    @PreAuthorize("@ss.hasPermi('system:bed:list')")
    @GetMapping("/trend")
    public AjaxResult trend() {
        return success(medicalBedMapper.selectBedTrend());
    }

    @PreAuthorize("@ss.hasPermi('system:bed:list')")
    @GetMapping("/usage-rate")
    public AjaxResult usageRate(Long orgId, Integer year) {
        return success(medicalBedMapper.selectBedUsageRate(orgId, year));
    }

}
