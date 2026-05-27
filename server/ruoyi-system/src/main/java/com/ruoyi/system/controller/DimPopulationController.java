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
import com.ruoyi.system.domain.DimPopulation;
import com.ruoyi.system.mapper.DimPopulationMapper;
import com.ruoyi.system.service.IDimPopulationService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * ?????Controller
 * 
 * @author ruoyi
 * @date 2026-05-27
 */
@RestController
@RequestMapping("/system/population")
public class DimPopulationController extends BaseController
{
    @Autowired
    private IDimPopulationService dimPopulationService;
    @Autowired
    private DimPopulationMapper dimPopulationMapper;


    /**
     * 查询?????列表
     */
    @PreAuthorize("@ss.hasPermi('system:population:list')")
    @GetMapping("/list")
    public TableDataInfo list(DimPopulation dimPopulation)
    {
        startPage();
        List<DimPopulation> list = dimPopulationService.selectDimPopulationList(dimPopulation);
        return getDataTable(list);
    }

    /**
     * 导出?????列表
     */
    @PreAuthorize("@ss.hasPermi('system:population:export')")
    @Log(title = "?????", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, DimPopulation dimPopulation)
    {
        List<DimPopulation> list = dimPopulationService.selectDimPopulationList(dimPopulation);
        ExcelUtil<DimPopulation> util = new ExcelUtil<DimPopulation>(DimPopulation.class);
        util.exportExcel(response, list, "?????数据");
    }

    /**
     * 获取?????详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:population:query')")
    @GetMapping("/detail/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(dimPopulationService.selectDimPopulationById(id));
    }

    /**
     * 新增?????
     */
    @PreAuthorize("@ss.hasPermi('system:population:add')")
    @Log(title = "?????", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody DimPopulation dimPopulation)
    {
        return toAjax(dimPopulationService.insertDimPopulation(dimPopulation));
    }

    /**
     * 修改?????
     */
    @PreAuthorize("@ss.hasPermi('system:population:edit')")
    @Log(title = "?????", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody DimPopulation dimPopulation)
    {
        return toAjax(dimPopulationService.updateDimPopulation(dimPopulation));
    }

    /**
     * 删除?????
     */
    @PreAuthorize("@ss.hasPermi('system:population:remove')")
    @Log(title = "?????", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(dimPopulationService.deleteDimPopulationByIds(ids));
    }
    // ====== 统计分析接口 ======

    @PreAuthorize("@ss.hasPermi('system:population:list')")
    @GetMapping("/summary")
    public AjaxResult summary(Long regionId, Integer year) {
        return success(dimPopulationMapper.selectPopulationSummary(regionId, year));
    }

    @PreAuthorize("@ss.hasPermi('system:population:list')")
    @GetMapping("/age-distribution")
    public AjaxResult ageDistribution(Long regionId, Integer year) {
        return success(dimPopulationMapper.selectPopulationAgeDistribution(regionId, year));
    }

    @PreAuthorize("@ss.hasPermi('system:population:list')")
    @GetMapping("/gender-ratio")
    public AjaxResult genderRatio(Long regionId, Integer year) {
        return success(dimPopulationMapper.selectPopulationGenderRatio(regionId, year));
    }

    @PreAuthorize("@ss.hasPermi('system:population:list')")
    @GetMapping("/region-distribution")
    public AjaxResult regionDistribution(Integer year) {
        return success(dimPopulationMapper.selectPopulationRegionDistribution(year));
    }

    @PreAuthorize("@ss.hasPermi('system:population:list')")
    @GetMapping("/trend")
    public AjaxResult trend() {
        return success(dimPopulationMapper.selectPopulationTrend());
    }

}
