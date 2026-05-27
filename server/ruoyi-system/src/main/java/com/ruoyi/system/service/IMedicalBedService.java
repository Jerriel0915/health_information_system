package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.MedicalBed;

/**
 * ???????Service接口
 * 
 * @author ruoyi
 * @date 2026-05-27
 */
public interface IMedicalBedService 
{
    /**
     * 查询???????
     * 
     * @param id ???????主键
     * @return ???????
     */
    public MedicalBed selectMedicalBedById(Long id);

    /**
     * 查询???????列表
     * 
     * @param medicalBed ???????
     * @return ???????集合
     */
    public List<MedicalBed> selectMedicalBedList(MedicalBed medicalBed);

    /**
     * 新增???????
     * 
     * @param medicalBed ???????
     * @return 结果
     */
    public int insertMedicalBed(MedicalBed medicalBed);

    /**
     * 修改???????
     * 
     * @param medicalBed ???????
     * @return 结果
     */
    public int updateMedicalBed(MedicalBed medicalBed);

    /**
     * 批量删除???????
     * 
     * @param ids 需要删除的???????主键集合
     * @return 结果
     */
    public int deleteMedicalBedByIds(Long[] ids);

    /**
     * 删除???????信息
     * 
     * @param id ???????主键
     * @return 结果
     */
    public int deleteMedicalBedById(Long id);
}
