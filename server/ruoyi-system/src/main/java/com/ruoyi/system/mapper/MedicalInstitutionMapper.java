package com.ruoyi.system.mapper;

import java.util.List;
import java.util.Map;
import com.ruoyi.system.domain.MedicalInstitution;
import org.apache.ibatis.annotations.Param;

public interface MedicalInstitutionMapper 
{
    public MedicalInstitution selectMedicalInstitutionById(Long id);

    public List<MedicalInstitution> selectMedicalInstitutionList(MedicalInstitution medicalInstitution);

    public int insertMedicalInstitution(MedicalInstitution medicalInstitution);

    public int updateMedicalInstitution(MedicalInstitution medicalInstitution);

    public int deleteMedicalInstitutionById(Long id);

    public int deleteMedicalInstitutionByIds(Long[] ids);

    // ====== 统计分析 ======

    Map<String, Object> selectInstitutionSummary(@Param("regionId") Long regionId);

    List<Map<String, Object>> selectInstitutionTypeDistribution(@Param("regionId") Long regionId);

    List<Map<String, Object>> selectInstitutionLevelDistribution(@Param("regionId") Long regionId);

    List<Map<String, Object>> selectInstitutionRegionDistribution();

    List<Map<String, Object>> selectInstitutionTrend();
}