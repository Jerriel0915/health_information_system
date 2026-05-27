package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.DimPopulationMapper;
import com.ruoyi.system.domain.DimPopulation;
import com.ruoyi.system.service.IDimPopulationService;

/**
 * ?????Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-27
 */
@Service
public class DimPopulationServiceImpl implements IDimPopulationService 
{
    @Autowired
    private DimPopulationMapper dimPopulationMapper;

    /**
     * 查询?????
     * 
     * @param id ?????主键
     * @return ?????
     */
    @Override
    public DimPopulation selectDimPopulationById(Long id)
    {
        return dimPopulationMapper.selectDimPopulationById(id);
    }

    /**
     * 查询?????列表
     * 
     * @param dimPopulation ?????
     * @return ?????
     */
    @Override
    public List<DimPopulation> selectDimPopulationList(DimPopulation dimPopulation)
    {
        return dimPopulationMapper.selectDimPopulationList(dimPopulation);
    }

    /**
     * 新增?????
     * 
     * @param dimPopulation ?????
     * @return 结果
     */
    @Override
    public int insertDimPopulation(DimPopulation dimPopulation)
    {
        return dimPopulationMapper.insertDimPopulation(dimPopulation);
    }

    /**
     * 修改?????
     * 
     * @param dimPopulation ?????
     * @return 结果
     */
    @Override
    public int updateDimPopulation(DimPopulation dimPopulation)
    {
        return dimPopulationMapper.updateDimPopulation(dimPopulation);
    }

    /**
     * 批量删除?????
     * 
     * @param ids 需要删除的?????主键
     * @return 结果
     */
    @Override
    public int deleteDimPopulationByIds(Long[] ids)
    {
        return dimPopulationMapper.deleteDimPopulationByIds(ids);
    }

    /**
     * 删除?????信息
     * 
     * @param id ?????主键
     * @return 结果
     */
    @Override
    public int deleteDimPopulationById(Long id)
    {
        return dimPopulationMapper.deleteDimPopulationById(id);
    }
}
