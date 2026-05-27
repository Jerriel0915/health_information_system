package com.ruoyi.system.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.MedicalCost;

/**
 * ?????Mapper接口
 * 
 * @author ruoyi
 * @date 2026-05-27
 */
public interface MedicalCostMapper 
{
    /**
     * 查询?????
     * 
     * @param id ?????主键
     * @return ?????
     */
    public MedicalCost selectMedicalCostById(Long id);

    /**
     * 查询?????列表
     * 
     * @param medicalCost ?????
     * @return ?????集合
     */
    public List<MedicalCost> selectMedicalCostList(MedicalCost medicalCost);

    /**
     * 新增?????
     * 
     * @param medicalCost ?????
     * @return 结果
     */
    public int insertMedicalCost(MedicalCost medicalCost);

    /**
     * 修改?????
     * 
     * @param medicalCost ?????
     * @return 结果
     */
    public int updateMedicalCost(MedicalCost medicalCost);

    /**
     * 删除?????
     * 
     * @param id ?????主键
     * @return 结果
     */
    public int deleteMedicalCostById(Long id);

    /**
     * 批量删除?????
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMedicalCostByIds(Long[] ids);

    // ====== 统计分析 ======

    Map<String, Object> selectCostSummary(@Param("orgId") Long orgId, @Param("year") Integer year);

    Map<String, Object> selectCostComposition(@Param("orgId") Long orgId, @Param("year") Integer year);

    List<Map<String, Object>> selectInsuranceAnalysis(@Param("orgId") Long orgId, @Param("year") Integer year);

    List<Map<String, Object>> selectAvgCostTrend();

    List<Map<String, Object>> selectCostTrend();

    List<Map<String, Object>> selectCostCategoryAnalysis(@Param("orgId") Long orgId, @Param("year") Integer year);
}
