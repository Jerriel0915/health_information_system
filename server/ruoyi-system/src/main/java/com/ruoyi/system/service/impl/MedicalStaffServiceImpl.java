package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.MedicalStaffMapper;
import com.ruoyi.system.domain.MedicalStaff;
import com.ruoyi.system.service.IMedicalStaffService;

/**
 * ???????Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-27
 */
@Service
public class MedicalStaffServiceImpl implements IMedicalStaffService 
{
    @Autowired
    private MedicalStaffMapper medicalStaffMapper;

    /**
     * 查询???????
     * 
     * @param id ???????主键
     * @return ???????
     */
    @Override
    public MedicalStaff selectMedicalStaffById(Long id)
    {
        return medicalStaffMapper.selectMedicalStaffById(id);
    }

    /**
     * 查询???????列表
     * 
     * @param medicalStaff ???????
     * @return ???????
     */
    @Override
    public List<MedicalStaff> selectMedicalStaffList(MedicalStaff medicalStaff)
    {
        return medicalStaffMapper.selectMedicalStaffList(medicalStaff);
    }

    /**
     * 新增???????
     * 
     * @param medicalStaff ???????
     * @return 结果
     */
    @Override
    public int insertMedicalStaff(MedicalStaff medicalStaff)
    {
        return medicalStaffMapper.insertMedicalStaff(medicalStaff);
    }

    /**
     * 修改???????
     * 
     * @param medicalStaff ???????
     * @return 结果
     */
    @Override
    public int updateMedicalStaff(MedicalStaff medicalStaff)
    {
        return medicalStaffMapper.updateMedicalStaff(medicalStaff);
    }

    /**
     * 批量删除???????
     * 
     * @param ids 需要删除的???????主键
     * @return 结果
     */
    @Override
    public int deleteMedicalStaffByIds(Long[] ids)
    {
        return medicalStaffMapper.deleteMedicalStaffByIds(ids);
    }

    /**
     * 删除???????信息
     * 
     * @param id ???????主键
     * @return 结果
     */
    @Override
    public int deleteMedicalStaffById(Long id)
    {
        return medicalStaffMapper.deleteMedicalStaffById(id);
    }
}
