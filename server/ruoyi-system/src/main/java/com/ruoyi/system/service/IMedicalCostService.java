package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.MedicalCost;

/**
 * ?????Service接口
 * 
 * @author ruoyi
 * @date 2026-05-27
 */
public interface IMedicalCostService 
{
    /**
     * 查询?????
     * 
     * @param id ?????主键
     * @return ?????
     */
    public MedicalCost selectMedicalCostById(Long id);

    /**
     * 查询?????列表
     * 
     * @param medicalCost ?????
     * @return ?????集合
     */
    public List<MedicalCost> selectMedicalCostList(MedicalCost medicalCost);

    /**
     * 新增?????
     * 
     * @param medicalCost ?????
     * @return 结果
     */
    public int insertMedicalCost(MedicalCost medicalCost);

    /**
     * 修改?????
     * 
     * @param medicalCost ?????
     * @return 结果
     */
    public int updateMedicalCost(MedicalCost medicalCost);

    /**
     * 批量删除?????
     * 
     * @param ids 需要删除的?????主键集合
     * @return 结果
     */
    public int deleteMedicalCostByIds(Long[] ids);

    /**
     * 删除?????信息
     * 
     * @param id ?????主键
     * @return 结果
     */
    public int deleteMedicalCostById(Long id);
}
