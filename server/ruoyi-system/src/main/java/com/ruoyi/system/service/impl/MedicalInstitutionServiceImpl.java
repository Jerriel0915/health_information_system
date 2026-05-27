package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.MedicalInstitutionMapper;
import com.ruoyi.system.domain.MedicalInstitution;
import com.ruoyi.system.service.IMedicalInstitutionService;

/**
 * ???????Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-27
 */
@Service
public class MedicalInstitutionServiceImpl implements IMedicalInstitutionService 
{
    @Autowired
    private MedicalInstitutionMapper medicalInstitutionMapper;

    /**
     * 查询???????
     * 
     * @param id ???????主键
     * @return ???????
     */
    @Override
    public MedicalInstitution selectMedicalInstitutionById(Long id)
    {
        return medicalInstitutionMapper.selectMedicalInstitutionById(id);
    }

    /**
     * 查询???????列表
     * 
     * @param medicalInstitution ???????
     * @return ???????
     */
    @Override
    public List<MedicalInstitution> selectMedicalInstitutionList(MedicalInstitution medicalInstitution)
    {
        return medicalInstitutionMapper.selectMedicalInstitutionList(medicalInstitution);
    }

    /**
     * 新增???????
     * 
     * @param medicalInstitution ???????
     * @return 结果
     */
    @Override
    public int insertMedicalInstitution(MedicalInstitution medicalInstitution)
    {
        return medicalInstitutionMapper.insertMedicalInstitution(medicalInstitution);
    }

    /**
     * 修改???????
     * 
     * @param medicalInstitution ???????
     * @return 结果
     */
    @Override
    public int updateMedicalInstitution(MedicalInstitution medicalInstitution)
    {
        return medicalInstitutionMapper.updateMedicalInstitution(medicalInstitution);
    }

    /**
     * 批量删除???????
     * 
     * @param ids 需要删除的???????主键
     * @return 结果
     */
    @Override
    public int deleteMedicalInstitutionByIds(Long[] ids)
    {
        return medicalInstitutionMapper.deleteMedicalInstitutionByIds(ids);
    }

    /**
     * 删除???????信息
     * 
     * @param id ???????主键
     * @return 结果
     */
    @Override
    public int deleteMedicalInstitutionById(Long id)
    {
        return medicalInstitutionMapper.deleteMedicalInstitutionById(id);
    }
}
