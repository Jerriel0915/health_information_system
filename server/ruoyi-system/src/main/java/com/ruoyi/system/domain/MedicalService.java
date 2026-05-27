package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * ???????对象 medical_service
 * 
 * @author ruoyi
 * @date 2026-05-27
 */
public class MedicalService extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ?? */
    private Long id;

    /** ????? */
    @Excel(name = "?????")
    private String serviceCode;

    /** ???? ID */
    @Excel(name = "???? ID")
    private Long orgId;

    /** ???? */
    @Excel(name = "????")
    private String patientName;

    /** ???? */
    @Excel(name = "????")
    private Long patientGender;

    /** ???? */
    @Excel(name = "????")
    private Long patientAge;

    /** ???? */
    @Excel(name = "????")
    private String serviceType;

    /** ???? */
    @Excel(name = "????")
    private String department;

    /** ?????ICD-10? */
    @Excel(name = "?????ICD-10?")
    private String diagnosisCode;

    /** ???? */
    @Excel(name = "????")
    private String diagnosisName;

    /** ???? ID */
    @Excel(name = "???? ID")
    private Long doctorId;

    /** ???? */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "????", width = 30, dateFormat = "yyyy-MM-dd")
    private Date serviceDate;

    /** ???? */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "????", width = 30, dateFormat = "yyyy-MM-dd")
    private Date dischargeDate;

    /** ???? */
    @Excel(name = "????")
    private Long daysInHospital;

    /** ?? */
    @Excel(name = "??")
    private Long serviceStatus;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private Date createdAt;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private Date updatedAt;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setServiceCode(String serviceCode) 
    {
        this.serviceCode = serviceCode;
    }

    public String getServiceCode() 
    {
        return serviceCode;
    }

    public void setOrgId(Long orgId) 
    {
        this.orgId = orgId;
    }

    public Long getOrgId() 
    {
        return orgId;
    }

    public void setPatientName(String patientName) 
    {
        this.patientName = patientName;
    }

    public String getPatientName() 
    {
        return patientName;
    }

    public void setPatientGender(Long patientGender) 
    {
        this.patientGender = patientGender;
    }

    public Long getPatientGender() 
    {
        return patientGender;
    }

    public void setPatientAge(Long patientAge) 
    {
        this.patientAge = patientAge;
    }

    public Long getPatientAge() 
    {
        return patientAge;
    }

    public void setServiceType(String serviceType) 
    {
        this.serviceType = serviceType;
    }

    public String getServiceType() 
    {
        return serviceType;
    }

    public void setDepartment(String department) 
    {
        this.department = department;
    }

    public String getDepartment() 
    {
        return department;
    }

    public void setDiagnosisCode(String diagnosisCode) 
    {
        this.diagnosisCode = diagnosisCode;
    }

    public String getDiagnosisCode() 
    {
        return diagnosisCode;
    }

    public void setDiagnosisName(String diagnosisName) 
    {
        this.diagnosisName = diagnosisName;
    }

    public String getDiagnosisName() 
    {
        return diagnosisName;
    }

    public void setDoctorId(Long doctorId) 
    {
        this.doctorId = doctorId;
    }

    public Long getDoctorId() 
    {
        return doctorId;
    }

    public void setServiceDate(Date serviceDate) 
    {
        this.serviceDate = serviceDate;
    }

    public Date getServiceDate() 
    {
        return serviceDate;
    }

    public void setDischargeDate(Date dischargeDate) 
    {
        this.dischargeDate = dischargeDate;
    }

    public Date getDischargeDate() 
    {
        return dischargeDate;
    }

    public void setDaysInHospital(Long daysInHospital) 
    {
        this.daysInHospital = daysInHospital;
    }

    public Long getDaysInHospital() 
    {
        return daysInHospital;
    }

    public void setServiceStatus(Long serviceStatus) 
    {
        this.serviceStatus = serviceStatus;
    }

    public Long getServiceStatus() 
    {
        return serviceStatus;
    }

    public void setCreatedAt(Date createdAt) 
    {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() 
    {
        return createdAt;
    }

    public void setUpdatedAt(Date updatedAt) 
    {
        this.updatedAt = updatedAt;
    }

    public Date getUpdatedAt() 
    {
        return updatedAt;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("serviceCode", getServiceCode())
            .append("orgId", getOrgId())
            .append("patientName", getPatientName())
            .append("patientGender", getPatientGender())
            .append("patientAge", getPatientAge())
            .append("serviceType", getServiceType())
            .append("department", getDepartment())
            .append("diagnosisCode", getDiagnosisCode())
            .append("diagnosisName", getDiagnosisName())
            .append("doctorId", getDoctorId())
            .append("serviceDate", getServiceDate())
            .append("dischargeDate", getDischargeDate())
            .append("daysInHospital", getDaysInHospital())
            .append("serviceStatus", getServiceStatus())
            .append("createdAt", getCreatedAt())
            .append("updatedAt", getUpdatedAt())
            .toString();
    }
}
