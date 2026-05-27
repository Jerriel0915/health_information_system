package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.DimRegionMapper;
import com.ruoyi.system.domain.DimRegion;
import com.ruoyi.system.service.IDimRegionService;

/**
 * ???????Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-27
 */
@Service
public class DimRegionServiceImpl implements IDimRegionService 
{
    @Autowired
    private DimRegionMapper dimRegionMapper;

    /**
     * 查询???????
     * 
     * @param id ???????主键
     * @return ???????
     */
    @Override
    public DimRegion selectDimRegionById(Long id)
    {
        return dimRegionMapper.selectDimRegionById(id);
    }

    /**
     * 查询???????列表
     * 
     * @param dimRegion ???????
     * @return ???????
     */
    @Override
    public List<DimRegion> selectDimRegionList(DimRegion dimRegion)
    {
        return dimRegionMapper.selectDimRegionList(dimRegion);
    }

    /**
     * 新增???????
     * 
     * @param dimRegion ???????
     * @return 结果
     */
    @Override
    public int insertDimRegion(DimRegion dimRegion)
    {
        return dimRegionMapper.insertDimRegion(dimRegion);
    }

    /**
     * 修改???????
     * 
     * @param dimRegion ???????
     * @return 结果
     */
    @Override
    public int updateDimRegion(DimRegion dimRegion)
    {
        return dimRegionMapper.updateDimRegion(dimRegion);
    }

    /**
     * 批量删除???????
     * 
     * @param ids 需要删除的???????主键
     * @return 结果
     */
    @Override
    public int deleteDimRegionByIds(Long[] ids)
    {
        return dimRegionMapper.deleteDimRegionByIds(ids);
    }

    /**
     * 删除???????信息
     * 
     * @param id ???????主键
     * @return 结果
     */
    @Override
    public int deleteDimRegionById(Long id)
    {
        return dimRegionMapper.deleteDimRegionById(id);
    }
}
