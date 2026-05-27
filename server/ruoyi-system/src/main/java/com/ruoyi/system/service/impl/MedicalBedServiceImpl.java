package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.MedicalBedMapper;
import com.ruoyi.system.domain.MedicalBed;
import com.ruoyi.system.service.IMedicalBedService;

/**
 * ???????Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-27
 */
@Service
public class MedicalBedServiceImpl implements IMedicalBedService 
{
    @Autowired
    private MedicalBedMapper medicalBedMapper;

    /**
     * 查询???????
     * 
     * @param id ???????主键
     * @return ???????
     */
    @Override
    public MedicalBed selectMedicalBedById(Long id)
    {
        return medicalBedMapper.selectMedicalBedById(id);
    }

    /**
     * 查询???????列表
     * 
     * @param medicalBed ???????
     * @return ???????
     */
    @Override
    public List<MedicalBed> selectMedicalBedList(MedicalBed medicalBed)
    {
        return medicalBedMapper.selectMedicalBedList(medicalBed);
    }

    /**
     * 新增???????
     * 
     * @param medicalBed ???????
     * @return 结果
     */
    @Override
    public int insertMedicalBed(MedicalBed medicalBed)
    {
        return medicalBedMapper.insertMedicalBed(medicalBed);
    }

    /**
     * 修改???????
     * 
     * @param medicalBed ???????
     * @return 结果
     */
    @Override
    public int updateMedicalBed(MedicalBed medicalBed)
    {
        return medicalBedMapper.updateMedicalBed(medicalBed);
    }

    /**
     * 批量删除???????
     * 
     * @param ids 需要删除的???????主键
     * @return 结果
     */
    @Override
    public int deleteMedicalBedByIds(Long[] ids)
    {
        return medicalBedMapper.deleteMedicalBedByIds(ids);
    }

    /**
     * 删除???????信息
     * 
     * @param id ???????主键
     * @return 结果
     */
    @Override
    public int deleteMedicalBedById(Long id)
    {
        return medicalBedMapper.deleteMedicalBedById(id);
    }
}
