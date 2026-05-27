package com.ruoyi.system.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.mapper.MedicalInstitutionMapper;
import com.ruoyi.system.mapper.MedicalStaffMapper;
import com.ruoyi.system.mapper.MedicalBedMapper;
import com.ruoyi.system.mapper.MedicalServiceMapper;
import com.ruoyi.system.mapper.MedicalCostMapper;

@RestController
@RequestMapping("/dashboard")
public class DashboardController extends BaseController
{
    @Autowired
    private MedicalInstitutionMapper medicalInstitutionMapper;

    @Autowired
    private MedicalStaffMapper medicalStaffMapper;

    @Autowired
    private MedicalBedMapper medicalBedMapper;

    @Autowired
    private MedicalServiceMapper medicalServiceMapper;

    @Autowired
    private MedicalCostMapper medicalCostMapper;

    @GetMapping("/summary")
    public AjaxResult summary() {
        Map<String, Object> institutionData = medicalInstitutionMapper.selectInstitutionSummary(null);
        Map<String, Object> staffData = medicalStaffMapper.selectStaffSummary(null);
        Map<String, Object> bedData = medicalBedMapper.selectBedSummary(null, null);
        Map<String, Object> serviceData = medicalServiceMapper.selectServiceSummary(null, null);
        Map<String, Object> costData = medicalCostMapper.selectCostSummary(null, null);

        Map<String, Object> result = new java.util.HashMap<>();
        if (institutionData != null) result.putAll(institutionData);
        if (staffData != null) result.putAll(staffData);
        if (bedData != null) result.putAll(bedData);
        if (serviceData != null) result.putAll(serviceData);
        if (costData != null) result.putAll(costData);
        return success(result);
    }
}