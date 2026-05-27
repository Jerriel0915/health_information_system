package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.MedicalStaff;

/**
 * ???????Service接口
 * 
 * @author ruoyi
 * @date 2026-05-27
 */
public interface IMedicalStaffService 
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
     * 批量删除???????
     * 
     * @param ids 需要删除的???????主键集合
     * @return 结果
     */
    public int deleteMedicalStaffByIds(Long[] ids);

    /**
     * 删除???????信息
     * 
     * @param id ???????主键
     * @return 结果
     */
    public int deleteMedicalStaffById(Long id);
}
