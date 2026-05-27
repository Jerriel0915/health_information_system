package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.DimPopulation;

/**
 * ?????Service接口
 * 
 * @author ruoyi
 * @date 2026-05-27
 */
public interface IDimPopulationService 
{
    /**
     * 查询?????
     * 
     * @param id ?????主键
     * @return ?????
     */
    public DimPopulation selectDimPopulationById(Long id);

    /**
     * 查询?????列表
     * 
     * @param dimPopulation ?????
     * @return ?????集合
     */
    public List<DimPopulation> selectDimPopulationList(DimPopulation dimPopulation);

    /**
     * 新增?????
     * 
     * @param dimPopulation ?????
     * @return 结果
     */
    public int insertDimPopulation(DimPopulation dimPopulation);

    /**
     * 修改?????
     * 
     * @param dimPopulation ?????
     * @return 结果
     */
    public int updateDimPopulation(DimPopulation dimPopulation);

    /**
     * 批量删除?????
     * 
     * @param ids 需要删除的?????主键集合
     * @return 结果
     */
    public int deleteDimPopulationByIds(Long[] ids);

    /**
     * 删除?????信息
     * 
     * @param id ?????主键
     * @return 结果
     */
    public int deleteDimPopulationById(Long id);
}
