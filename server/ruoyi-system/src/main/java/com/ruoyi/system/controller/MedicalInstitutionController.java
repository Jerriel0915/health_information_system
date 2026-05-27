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
import com.ruoyi.system.domain.MedicalInstitution;
import com.ruoyi.system.service.IMedicalInstitutionService;
import com.ruoyi.system.mapper.MedicalInstitutionMapper;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

@RestController
@RequestMapping("/system/institution")
public class MedicalInstitutionController extends BaseController
{
    @Autowired
    private IMedicalInstitutionService medicalInstitutionService;

    @Autowired
    private MedicalInstitutionMapper medicalInstitutionMapper;

    @PreAuthorize("@ss.hasPermi('system:institution:list')")
    @GetMapping("/list")
    public TableDataInfo list(MedicalInstitution medicalInstitution)
    {
        startPage();
        List<MedicalInstitution> list = medicalInstitutionService.selectMedicalInstitutionList(medicalInstitution);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('system:institution:export')")
    @Log(title = "医疗机构", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MedicalInstitution medicalInstitution)
    {
        List<MedicalInstitution> list = medicalInstitutionService.selectMedicalInstitutionList(medicalInstitution);
        ExcelUtil<MedicalInstitution> util = new ExcelUtil<MedicalInstitution>(MedicalInstitution.class);
        util.exportExcel(response, list, "医疗机构数据");
    }

    @PreAuthorize("@ss.hasPermi('system:institution:query')")
    @GetMapping("/detail/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(medicalInstitutionService.selectMedicalInstitutionById(id));
    }

    @PreAuthorize("@ss.hasPermi('system:institution:add')")
    @Log(title = "医疗机构", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MedicalInstitution medicalInstitution)
    {
        return toAjax(medicalInstitutionService.insertMedicalInstitution(medicalInstitution));
    }

    @PreAuthorize("@ss.hasPermi('system:institution:edit')")
    @Log(title = "医疗机构", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MedicalInstitution medicalInstitution)
    {
        return toAjax(medicalInstitutionService.updateMedicalInstitution(medicalInstitution));
    }

    @PreAuthorize("@ss.hasPermi('system:institution:remove')")
    @Log(title = "医疗机构", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(medicalInstitutionService.deleteMedicalInstitutionByIds(ids));
    }

    // ====== 统计分析接口 ======

    @PreAuthorize("@ss.hasPermi('system:institution:list')")
    @GetMapping("/summary")
    public AjaxResult summary(Long regionId)
    {
        return success(medicalInstitutionMapper.selectInstitutionSummary(regionId));
    }

    @PreAuthorize("@ss.hasPermi('system:institution:list')")
    @GetMapping("/type-distribution")
    public AjaxResult typeDistribution(Long regionId)
    {
        return success(medicalInstitutionMapper.selectInstitutionTypeDistribution(regionId));
    }

    @PreAuthorize("@ss.hasPermi('system:institution:list')")
    @GetMapping("/level-distribution")
    public AjaxResult levelDistribution(Long regionId)
    {
        return success(medicalInstitutionMapper.selectInstitutionLevelDistribution(regionId));
    }

    @PreAuthorize("@ss.hasPermi('system:institution:list')")
    @GetMapping("/region-distribution")
    public AjaxResult regionDistribution()
    {
        return success(medicalInstitutionMapper.selectInstitutionRegionDistribution());
    }

    @PreAuthorize("@ss.hasPermi('system:institution:list')")
    @GetMapping("/trend")
    public AjaxResult trend()
    {
        return success(medicalInstitutionMapper.selectInstitutionTrend());
    }
}
