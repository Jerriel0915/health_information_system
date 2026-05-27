package com.ruoyi.system.controller;

import java.util.List;
import java.util.Map;
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
import com.ruoyi.system.domain.MedicalStaff;
import com.ruoyi.system.mapper.MedicalStaffMapper;
import com.ruoyi.system.service.IMedicalStaffService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * ???????Controller
 * 
 * @author ruoyi
 * @date 2026-05-27
 */
@RestController
@RequestMapping("/system/staff")
public class MedicalStaffController extends BaseController
{
    @Autowired
    private IMedicalStaffService medicalStaffService;

    
    @Autowired
    private MedicalStaffMapper medicalStaffMapper;


    /**
     * 查询???????列表
     */
    @PreAuthorize("@ss.hasPermi('system:staff:list')")
    @GetMapping("/list")
    public TableDataInfo list(MedicalStaff medicalStaff)
    {
        startPage();
        List<MedicalStaff> list = medicalStaffService.selectMedicalStaffList(medicalStaff);
        return getDataTable(list);
    }

    /**
     * 导出???????列表
     */
    @PreAuthorize("@ss.hasPermi('system:staff:export')")
    @Log(title = "???????", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MedicalStaff medicalStaff)
    {
        List<MedicalStaff> list = medicalStaffService.selectMedicalStaffList(medicalStaff);
        ExcelUtil<MedicalStaff> util = new ExcelUtil<MedicalStaff>(MedicalStaff.class);
        util.exportExcel(response, list, "???????数据");
    }

    /**
     * 获取???????详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:staff:query')")
    @GetMapping("/detail/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(medicalStaffService.selectMedicalStaffById(id));
    }

    /**
     * 新增???????
     */
    @PreAuthorize("@ss.hasPermi('system:staff:add')")
    @Log(title = "???????", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MedicalStaff medicalStaff)
    {
        return toAjax(medicalStaffService.insertMedicalStaff(medicalStaff));
    }

    /**
     * 修改???????
     */
    @PreAuthorize("@ss.hasPermi('system:staff:edit')")
    @Log(title = "???????", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MedicalStaff medicalStaff)
    {
        return toAjax(medicalStaffService.updateMedicalStaff(medicalStaff));
    }

    /**
     * 删除???????
     */
    @PreAuthorize("@ss.hasPermi('system:staff:remove')")
    @Log(title = "???????", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(medicalStaffService.deleteMedicalStaffByIds(ids));
    }
    // ====== 统计分析接口 ======

    @PreAuthorize("@ss.hasPermi('system:staff:list')")
    @GetMapping("/summary")
    public AjaxResult summary(Long orgId) {
        return success(medicalStaffMapper.selectStaffSummary(orgId));
    }

    @PreAuthorize("@ss.hasPermi('system:staff:list')")
    @GetMapping("/job-title-distribution")
    public AjaxResult jobTitleDistribution(Long orgId) {
        return success(medicalStaffMapper.selectStaffJobTitleDistribution(orgId));
    }

    @PreAuthorize("@ss.hasPermi('system:staff:list')")
    @GetMapping("/category-distribution")
    public AjaxResult categoryDistribution(Long orgId) {
        return success(medicalStaffMapper.selectStaffCategoryDistribution(orgId));
    }

    @PreAuthorize("@ss.hasPermi('system:staff:list')")
    @GetMapping("/education-distribution")
    public AjaxResult educationDistribution(Long orgId) {
        return success(medicalStaffMapper.selectStaffEducationDistribution(orgId));
    }

    @PreAuthorize("@ss.hasPermi('system:staff:list')")
    @GetMapping("/gender-ratio")
    public AjaxResult genderRatio(Long orgId) {
        return success(medicalStaffMapper.selectStaffGenderRatio(orgId));
    }

    @PreAuthorize("@ss.hasPermi('system:staff:list')")
    @GetMapping("/trend")
    public AjaxResult trend() {
        return success(medicalStaffMapper.selectStaffTrend());
    }

}
