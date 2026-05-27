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
import com.ruoyi.system.domain.MedicalCost;
import com.ruoyi.system.mapper.MedicalCostMapper;
import com.ruoyi.system.service.IMedicalCostService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * ?????Controller
 * 
 * @author ruoyi
 * @date 2026-05-27
 */
@RestController
@RequestMapping("/system/cost")
public class MedicalCostController extends BaseController
{
    @Autowired
    private IMedicalCostService medicalCostService;
    @Autowired
    private MedicalCostMapper medicalCostMapper;


    /**
     * 查询?????列表
     */
    @PreAuthorize("@ss.hasPermi('system:cost:list')")
    @GetMapping("/list")
    public TableDataInfo list(MedicalCost medicalCost)
    {
        startPage();
        List<MedicalCost> list = medicalCostService.selectMedicalCostList(medicalCost);
        return getDataTable(list);
    }

    /**
     * 导出?????列表
     */
    @PreAuthorize("@ss.hasPermi('system:cost:export')")
    @Log(title = "?????", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MedicalCost medicalCost)
    {
        List<MedicalCost> list = medicalCostService.selectMedicalCostList(medicalCost);
        ExcelUtil<MedicalCost> util = new ExcelUtil<MedicalCost>(MedicalCost.class);
        util.exportExcel(response, list, "?????数据");
    }

    /**
     * 获取?????详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:cost:query')")
    @GetMapping("/detail/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(medicalCostService.selectMedicalCostById(id));
    }

    /**
     * 新增?????
     */
    @PreAuthorize("@ss.hasPermi('system:cost:add')")
    @Log(title = "?????", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MedicalCost medicalCost)
    {
        return toAjax(medicalCostService.insertMedicalCost(medicalCost));
    }

    /**
     * 修改?????
     */
    @PreAuthorize("@ss.hasPermi('system:cost:edit')")
    @Log(title = "?????", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MedicalCost medicalCost)
    {
        return toAjax(medicalCostService.updateMedicalCost(medicalCost));
    }

    /**
     * 删除?????
     */
    @PreAuthorize("@ss.hasPermi('system:cost:remove')")
    @Log(title = "?????", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(medicalCostService.deleteMedicalCostByIds(ids));
    }
    // ====== 统计分析接口 ======

    @PreAuthorize("@ss.hasPermi('system:cost:list')")
    @GetMapping("/summary")
    public AjaxResult summary(Long orgId, Integer year) {
        return success(medicalCostMapper.selectCostSummary(orgId, year));
    }

    @PreAuthorize("@ss.hasPermi('system:cost:list')")
    @GetMapping("/composition")
    public AjaxResult composition(Long orgId, Integer year) {
        return success(medicalCostMapper.selectCostComposition(orgId, year));
    }

    @PreAuthorize("@ss.hasPermi('system:cost:list')")
    @GetMapping("/insurance-analysis")
    public AjaxResult insuranceAnalysis(Long orgId, Integer year) {
        return success(medicalCostMapper.selectInsuranceAnalysis(orgId, year));
    }

    @PreAuthorize("@ss.hasPermi('system:cost:list')")
    @GetMapping("/avg-cost-trend")
    public AjaxResult avgCostTrend() {
        return success(medicalCostMapper.selectAvgCostTrend());
    }

    @PreAuthorize("@ss.hasPermi('system:cost:list')")
    @GetMapping("/trend")
    public AjaxResult trend() {
        return success(medicalCostMapper.selectCostTrend());
    }

    @PreAuthorize("@ss.hasPermi('system:cost:list')")
    @GetMapping("/category-analysis")
    public AjaxResult categoryAnalysis(Long orgId, Integer year) {
        return success(medicalCostMapper.selectCostCategoryAnalysis(orgId, year));
    }

}
