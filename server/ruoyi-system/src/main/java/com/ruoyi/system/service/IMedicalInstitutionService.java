package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.MedicalInstitution;

/**
 * ???????Service接口
 * 
 * @author ruoyi
 * @date 2026-05-27
 */
public interface IMedicalInstitutionService 
{
    /**
     * 查询???????
     * 
     * @param id ???????主键
     * @return ???????
     */
    public MedicalInstitution selectMedicalInstitutionById(Long id);

    /**
     * 查询???????列表
     * 
     * @param medicalInstitution ???????
     * @return ???????集合
     */
    public List<MedicalInstitution> selectMedicalInstitutionList(MedicalInstitution medicalInstitution);

    /**
     * 新增???????
     * 
     * @param medicalInstitution ???????
     * @return 结果
     */
    public int insertMedicalInstitution(MedicalInstitution medicalInstitution);

    /**
     * 修改???????
     * 
     * @param medicalInstitution ???????
     * @return 结果
     */
    public int updateMedicalInstitution(MedicalInstitution medicalInstitution);

    /**
     * 批量删除???????
     * 
     * @param ids 需要删除的???????主键集合
     * @return 结果
     */
    public int deleteMedicalInstitutionByIds(Long[] ids);

    /**
     * 删除???????信息
     * 
     * @param id ???????主键
     * @return 结果
     */
    public int deleteMedicalInstitutionById(Long id);
}
