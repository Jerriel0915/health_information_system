package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.MedicalCostMapper;
import com.ruoyi.system.domain.MedicalCost;
import com.ruoyi.system.service.IMedicalCostService;

/**
 * ?????Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-27
 */
@Service
public class MedicalCostServiceImpl implements IMedicalCostService 
{
    @Autowired
    private MedicalCostMapper medicalCostMapper;

    /**
     * 查询?????
     * 
     * @param id ?????主键
     * @return ?????
     */
    @Override
    public MedicalCost selectMedicalCostById(Long id)
    {
        return medicalCostMapper.selectMedicalCostById(id);
    }

    /**
     * 查询?????列表
     * 
     * @param medicalCost ?????
     * @return ?????
     */
    @Override
    public List<MedicalCost> selectMedicalCostList(MedicalCost medicalCost)
    {
        return medicalCostMapper.selectMedicalCostList(medicalCost);
    }

    /**
     * 新增?????
     * 
     * @param medicalCost ?????
     * @return 结果
     */
    @Override
    public int insertMedicalCost(MedicalCost medicalCost)
    {
        return medicalCostMapper.insertMedicalCost(medicalCost);
    }

    /**
     * 修改?????
     * 
     * @param medicalCost ?????
     * @return 结果
     */
    @Override
    public int updateMedicalCost(MedicalCost medicalCost)
    {
        return medicalCostMapper.updateMedicalCost(medicalCost);
    }

    /**
     * 批量删除?????
     * 
     * @param ids 需要删除的?????主键
     * @return 结果
     */
    @Override
    public int deleteMedicalCostByIds(Long[] ids)
    {
        return medicalCostMapper.deleteMedicalCostByIds(ids);
    }

    /**
     * 删除?????信息
     * 
     * @param id ?????主键
     * @return 结果
     */
    @Override
    public int deleteMedicalCostById(Long id)
    {
        return medicalCostMapper.deleteMedicalCostById(id);
    }
}
