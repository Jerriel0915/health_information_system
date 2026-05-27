package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.DimRegion;

/**
 * ???????Service接口
 * 
 * @author ruoyi
 * @date 2026-05-27
 */
public interface IDimRegionService 
{
    /**
     * 查询???????
     * 
     * @param id ???????主键
     * @return ???????
     */
    public DimRegion selectDimRegionById(Long id);

    /**
     * 查询???????列表
     * 
     * @param dimRegion ???????
     * @return ???????集合
     */
    public List<DimRegion> selectDimRegionList(DimRegion dimRegion);

    /**
     * 新增???????
     * 
     * @param dimRegion ???????
     * @return 结果
     */
    public int insertDimRegion(DimRegion dimRegion);

    /**
     * 修改???????
     * 
     * @param dimRegion ???????
     * @return 结果
     */
    public int updateDimRegion(DimRegion dimRegion);

    /**
     * 批量删除???????
     * 
     * @param ids 需要删除的???????主键集合
     * @return 结果
     */
    public int deleteDimRegionByIds(Long[] ids);

    /**
     * 删除???????信息
     * 
     * @param id ???????主键
     * @return 结果
     */
    public int deleteDimRegionById(Long id);
}
