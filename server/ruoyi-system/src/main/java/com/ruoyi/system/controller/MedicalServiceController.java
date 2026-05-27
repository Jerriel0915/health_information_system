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
import com.ruoyi.system.domain.MedicalService;
import com.ruoyi.system.mapper.MedicalServiceMapper;
import com.ruoyi.system.service.IMedicalServiceService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * ???????Controller
 * 
 * @author ruoyi
 * @date 2026-05-27
 */
@RestController
@RequestMapping("/system/service")
public class MedicalServiceController extends BaseController
{
    @Autowired
    private IMedicalServiceService medicalServiceService;
    @Autowired
    private MedicalServiceMapper medicalServiceMapper;


    /**
     * 查询???????列表
     */
    @PreAuthorize("@ss.hasPermi('system:service:list')")
    @GetMapping("/list")
    public TableDataInfo list(MedicalService medicalService)
    {
        startPage();
        List<MedicalService> list = medicalServiceService.selectMedicalServiceList(medicalService);
        return getDataTable(list);
    }

    /**
     * 导出???????列表
     */
    @PreAuthorize("@ss.hasPermi('system:service:export')")
    @Log(title = "???????", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MedicalService medicalService)
    {
        List<MedicalService> list = medicalServiceService.selectMedicalServiceList(medicalService);
        ExcelUtil<MedicalService> util = new ExcelUtil<MedicalService>(MedicalService.class);
        util.exportExcel(response, list, "???????数据");
    }

    /**
     * 获取???????详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:service:query')")
    @GetMapping("/detail/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(medicalServiceService.selectMedicalServiceById(id));
    }

    /**
     * 新增???????
     */
    @PreAuthorize("@ss.hasPermi('system:service:add')")
    @Log(title = "???????", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MedicalService medicalService)
    {
        return toAjax(medicalServiceService.insertMedicalService(medicalService));
    }

    /**
     * 修改???????
     */
    @PreAuthorize("@ss.hasPermi('system:service:edit')")
    @Log(title = "???????", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MedicalService medicalService)
    {
        return toAjax(medicalServiceService.updateMedicalService(medicalService));
    }

    /**
     * 删除???????
     */
    @PreAuthorize("@ss.hasPermi('system:service:remove')")
    @Log(title = "???????", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(medicalServiceService.deleteMedicalServiceByIds(ids));
    }
    // ====== 统计分析接口 ======

    @PreAuthorize("@ss.hasPermi('system:service:list')")
    @GetMapping("/summary")
    public AjaxResult summary(Long orgId, Integer year) {
        return success(medicalServiceMapper.selectServiceSummary(orgId, year));
    }

    @PreAuthorize("@ss.hasPermi('system:service:list')")
    @GetMapping("/type-distribution")
    public AjaxResult typeDistribution(Long orgId, Integer year) {
        return success(medicalServiceMapper.selectServiceTypeDistribution(orgId, year));
    }

    @PreAuthorize("@ss.hasPermi('system:service:list')")
    @GetMapping("/dept-distribution")
    public AjaxResult deptDistribution(Long orgId, Integer year) {
        return success(medicalServiceMapper.selectServiceDeptDistribution(orgId, year));
    }

    @PreAuthorize("@ss.hasPermi('system:service:list')")
    @GetMapping("/diagnosis-ranking")
    public AjaxResult diagnosisRanking(Long orgId, Integer year, Integer topN) {
        return success(medicalServiceMapper.selectDiagnosisRanking(orgId, year, topN));
    }

    @PreAuthorize("@ss.hasPermi('system:service:list')")
    @GetMapping("/trend")
    public AjaxResult trend() {
        return success(medicalServiceMapper.selectServiceTrend());
    }

    @PreAuthorize("@ss.hasPermi('system:service:list')")
    @GetMapping("/avg-days")
    public AjaxResult avgDays(Long orgId, Integer year) {
        return success(medicalServiceMapper.selectServiceAvgDays(orgId, year));
    }

}
