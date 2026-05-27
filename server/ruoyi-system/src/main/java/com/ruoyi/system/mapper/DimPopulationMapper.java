package com.ruoyi.system.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.DimPopulation;

/**
 * ?????Mapper接口
 * 
 * @author ruoyi
 * @date 2026-05-27
 */
public interface DimPopulationMapper 
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
     * 删除?????
     * 
     * @param id ?????主键
     * @return 结果
     */
    public int deleteDimPopulationById(Long id);

    /**
     * 批量删除?????
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteDimPopulationByIds(Long[] ids);

    // ====== 统计分析 ======

    Map<String, Object> selectPopulationSummary(@Param("regionId") Long regionId, @Param("year") Integer year);

    Map<String, Object> selectPopulationAgeDistribution(@Param("regionId") Long regionId, @Param("year") Integer year);

    Map<String, Object> selectPopulationGenderRatio(@Param("regionId") Long regionId, @Param("year") Integer year);

    List<Map<String, Object>> selectPopulationRegionDistribution(@Param("year") Integer year);

    List<Map<String, Object>> selectPopulationTrend();
}
