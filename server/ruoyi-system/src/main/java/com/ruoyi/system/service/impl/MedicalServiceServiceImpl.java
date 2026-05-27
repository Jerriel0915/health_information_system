package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.MedicalServiceMapper;
import com.ruoyi.system.domain.MedicalService;
import com.ruoyi.system.service.IMedicalServiceService;

/**
 * ???????Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-27
 */
@Service
public class MedicalServiceServiceImpl implements IMedicalServiceService 
{
    @Autowired
    private MedicalServiceMapper medicalServiceMapper;

    /**
     * 查询???????
     * 
     * @param id ???????主键
     * @return ???????
     */
    @Override
    public MedicalService selectMedicalServiceById(Long id)
    {
        return medicalServiceMapper.selectMedicalServiceById(id);
    }

    /**
     * 查询???????列表
     * 
     * @param medicalService ???????
     * @return ???????
     */
    @Override
    public List<MedicalService> selectMedicalServiceList(MedicalService medicalService)
    {
        return medicalServiceMapper.selectMedicalServiceList(medicalService);
    }

    /**
     * 新增???????
     * 
     * @param medicalService ???????
     * @return 结果
     */
    @Override
    public int insertMedicalService(MedicalService medicalService)
    {
        return medicalServiceMapper.insertMedicalService(medicalService);
    }

    /**
     * 修改???????
     * 
     * @param medicalService ???????
     * @return 结果
     */
    @Override
    public int updateMedicalService(MedicalService medicalService)
    {
        return medicalServiceMapper.updateMedicalService(medicalService);
    }

    /**
     * 批量删除???????
     * 
     * @param ids 需要删除的???????主键
     * @return 结果
     */
    @Override
    public int deleteMedicalServiceByIds(Long[] ids)
    {
        return medicalServiceMapper.deleteMedicalServiceByIds(ids);
    }

    /**
     * 删除???????信息
     * 
     * @param id ???????主键
     * @return 结果
     */
    @Override
    public int deleteMedicalServiceById(Long id)
    {
        return medicalServiceMapper.deleteMedicalServiceById(id);
    }
}
