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
import com.ruoyi.system.domain.DimRegion;
import com.ruoyi.system.mapper.DimRegionMapper;
import com.ruoyi.system.service.IDimRegionService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * ???????Controller
 * 
 * @author ruoyi
 * @date 2026-05-27
 */
@RestController
@RequestMapping("/system/region")
public class DimRegionController extends BaseController
{
    @Autowired
    private IDimRegionService dimRegionService;
    @Autowired
    private DimRegionMapper dimRegionMapper;


    /**
     * 查询???????列表
     */
    @PreAuthorize("@ss.hasPermi('system:region:list')")
    @GetMapping("/list")
    public TableDataInfo list(DimRegion dimRegion)
    {
        startPage();
        List<DimRegion> list = dimRegionService.selectDimRegionList(dimRegion);
        return getDataTable(list);
    }

    /**
     * 导出???????列表
     */
    @PreAuthorize("@ss.hasPermi('system:region:export')")
    @Log(title = "???????", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, DimRegion dimRegion)
    {
        List<DimRegion> list = dimRegionService.selectDimRegionList(dimRegion);
        ExcelUtil<DimRegion> util = new ExcelUtil<DimRegion>(DimRegion.class);
        util.exportExcel(response, list, "???????数据");
    }

    /**
     * 获取???????详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:region:query')")
    @GetMapping("/detail/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(dimRegionService.selectDimRegionById(id));
    }

    /**
     * 新增???????
     */
    @PreAuthorize("@ss.hasPermi('system:region:add')")
    @Log(title = "???????", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody DimRegion dimRegion)
    {
        return toAjax(dimRegionService.insertDimRegion(dimRegion));
    }

    /**
     * 修改???????
     */
    @PreAuthorize("@ss.hasPermi('system:region:edit')")
    @Log(title = "???????", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody DimRegion dimRegion)
    {
        return toAjax(dimRegionService.updateDimRegion(dimRegion));
    }

    /**
     * 删除???????
     */
    @PreAuthorize("@ss.hasPermi('system:region:remove')")
    @Log(title = "???????", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(dimRegionService.deleteDimRegionByIds(ids));
    }
    // ====== 统计分析接口 ======

    @PreAuthorize("@ss.hasPermi('system:region:list')")
    @GetMapping("/tree")
    public AjaxResult tree() {
        return success(dimRegionMapper.selectRegionTree());
    }

    @PreAuthorize("@ss.hasPermi('system:region:list')")
    @GetMapping("/summary")
    public AjaxResult summary() {
        return success(dimRegionMapper.selectRegionSummary());
    }

}
