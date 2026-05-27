package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.MedicalService;

/**
 * ???????Service接口
 * 
 * @author ruoyi
 * @date 2026-05-27
 */
public interface IMedicalServiceService 
{
    /**
     * 查询???????
     * 
     * @param id ???????主键
     * @return ???????
     */
    public MedicalService selectMedicalServiceById(Long id);

    /**
     * 查询???????列表
     * 
     * @param medicalService ???????
     * @return ???????集合
     */
    public List<MedicalService> selectMedicalServiceList(MedicalService medicalService);

    /**
     * 新增???????
     * 
     * @param medicalService ???????
     * @return 结果
     */
    public int insertMedicalService(MedicalService medicalService);

    /**
     * 修改???????
     * 
     * @param medicalService ???????
     * @return 结果
     */
    public int updateMedicalService(MedicalService medicalService);

    /**
     * 批量删除???????
     * 
     * @param ids 需要删除的???????主键集合
     * @return 结果
     */
    public int deleteMedicalServiceByIds(Long[] ids);

    /**
     * 删除???????信息
     * 
     * @param id ???????主键
     * @return 结果
     */
    public int deleteMedicalServiceById(Long id);
}
