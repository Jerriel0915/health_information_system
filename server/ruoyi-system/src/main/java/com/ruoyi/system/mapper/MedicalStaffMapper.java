package com.ruoyi.system.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.MedicalStaff;

/**
 * ???????Mapper接口
 * 
 * @author ruoyi
 * @date 2026-05-27
 */
public interface MedicalStaffMapper 
{
    /**
     * 查询???????
     * 
     * @param id ???????主键
     * @return ???????
     */
    public MedicalStaff selectMedicalStaffById(Long id);

    /**
     * 查询???????列表
     * 
     * @param medicalStaff ???????
     * @return ???????集合
     */
    public List<MedicalStaff> selectMedicalStaffList(MedicalStaff medicalStaff);

    /**
     * 新增???????
     * 
     * @param medicalStaff ???????
     * @return 结果
     */
    public int insertMedicalStaff(MedicalStaff medicalStaff);

    /**
     * 修改???????
     * 
     * @param medicalStaff ???????
     * @return 结果
     */
    public int updateMedicalStaff(MedicalStaff medicalStaff);

    /**
     * 删除???????
     * 
     * @param id ???????主键
     * @return 结果
     */
    public int deleteMedicalStaffById(Long id);

    /**
     * 批量删除???????
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMedicalStaffByIds(Long[] ids);

    // ====== 统计分析 ======

    Map<String, Object> selectStaffSummary(@Param("orgId") Long orgId);

    List<Map<String, Object>> selectStaffJobTitleDistribution(@Param("orgId") Long orgId);

    List<Map<String, Object>> selectStaffCategoryDistribution(@Param("orgId") Long orgId);

    List<Map<String, Object>> selectStaffEducationDistribution(@Param("orgId") Long orgId);

    List<Map<String, Object>> selectStaffGenderRatio(@Param("orgId") Long orgId);

    List<Map<String, Object>> selectStaffTrend();
}
