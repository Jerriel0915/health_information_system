package com.ruoyi.system.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.MedicalService;

/**
 * ???????Mapper接口
 * 
 * @author ruoyi
 * @date 2026-05-27
 */
public interface MedicalServiceMapper 
{
    /**
     * 查询???????
     * 
     * @param id ???????主键
     * @return ???????
     */
    public MedicalService selectMedicalServiceById(Long id);

    /**
     * 查询???????列表
     * 
     * @param medicalService ???????
     * @return ???????集合
     */
    public List<MedicalService> selectMedicalServiceList(MedicalService medicalService);

    /**
     * 新增???????
     * 
     * @param medicalService ???????
     * @return 结果
     */
    public int insertMedicalService(MedicalService medicalService);

    /**
     * 修改???????
     * 
     * @param medicalService ???????
     * @return 结果
     */
    public int updateMedicalService(MedicalService medicalService);

    /**
     * 删除???????
     * 
     * @param id ???????主键
     * @return 结果
     */
    public int deleteMedicalServiceById(Long id);

    /**
     * 批量删除???????
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMedicalServiceByIds(Long[] ids);

    // ====== 统计分析 ======

    Map<String, Object> selectServiceSummary(@Param("orgId") Long orgId, @Param("year") Integer year);

    List<Map<String, Object>> selectServiceTypeDistribution(@Param("orgId") Long orgId, @Param("year") Integer year);

    List<Map<String, Object>> selectServiceDeptDistribution(@Param("orgId") Long orgId, @Param("year") Integer year);

    List<Map<String, Object>> selectDiagnosisRanking(@Param("orgId") Long orgId, @Param("year") Integer year, @Param("topN") Integer topN);

    List<Map<String, Object>> selectServiceTrend();

    List<Map<String, Object>> selectServiceAvgDays(@Param("orgId") Long orgId, @Param("year") Integer year);
}
