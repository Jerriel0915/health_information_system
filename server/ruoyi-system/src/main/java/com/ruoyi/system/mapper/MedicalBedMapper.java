package com.ruoyi.system.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.MedicalBed;

/**
 * ???????Mapper接口
 * 
 * @author ruoyi
 * @date 2026-05-27
 */
public interface MedicalBedMapper 
{
    /**
     * 查询???????
     * 
     * @param id ???????主键
     * @return ???????
     */
    public MedicalBed selectMedicalBedById(Long id);

    /**
     * 查询???????列表
     * 
     * @param medicalBed ???????
     * @return ???????集合
     */
    public List<MedicalBed> selectMedicalBedList(MedicalBed medicalBed);

    /**
     * 新增???????
     * 
     * @param medicalBed ???????
     * @return 结果
     */
    public int insertMedicalBed(MedicalBed medicalBed);

    /**
     * 修改???????
     * 
     * @param medicalBed ???????
     * @return 结果
     */
    public int updateMedicalBed(MedicalBed medicalBed);

    /**
     * 删除???????
     * 
     * @param id ???????主键
     * @return 结果
     */
    public int deleteMedicalBedById(Long id);

    /**
     * 批量删除???????
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMedicalBedByIds(Long[] ids);

    // ====== 统计分析 ======

    Map<String, Object> selectBedSummary(@Param("orgId") Long orgId, @Param("year") Integer year);

    List<Map<String, Object>> selectBedDeptDistribution(@Param("orgId") Long orgId, @Param("year") Integer year);

    List<Map<String, Object>> selectBedRegionDistribution();

    List<Map<String, Object>> selectBedTrend();

    List<Map<String, Object>> selectBedUsageRate(@Param("orgId") Long orgId, @Param("year") Integer year);
}
