package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class MedicalService extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    @Excel(name = "服务编码")
    private String serviceCode;

    @Excel(name = "机构ID")
    private Long orgId;

    @Excel(name = "机构名称")
    private String orgName;

    @Excel(name = "服务类别")
    private String serviceCategory;

    @Excel(name = "患者性别")
    private Long patientGender;

    @Excel(name = "患者年龄")
    private Long patientAge;

    @Excel(name = "服务类型")
    private String serviceType;

    @Excel(name = "科室")
    private String department;

    @Excel(name = "诊断编码")
    private String diagnosisCode;

    @Excel(name = "诊断名称")
    private String diagnosisName;

    @Excel(name = "医生ID")
    private Long doctorId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "服务日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date serviceDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "出院日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date dischargeDate;

    @Excel(name = "住院天数")
    private Long daysInHospital;

    @Excel(name = "服务状态")
    private Long serviceStatus;

    private Date createdAt;
    private Date updatedAt;

    public void setId(Long id) { this.id = id; }
    public Long getId() { return id; }
    public void setServiceCode(String serviceCode) { this.serviceCode = serviceCode; }
    public String getServiceCode() { return serviceCode; }
    public void setOrgId(Long orgId) { this.orgId = orgId; }
    public Long getOrgId() { return orgId; }
    public void setOrgName(String orgName) { this.orgName = orgName; }
    public String getOrgName() { return orgName; }
    public void setServiceCategory(String serviceCategory) { this.serviceCategory = serviceCategory; }
    public String getServiceCategory() { return serviceCategory; }
    public void setPatientGender(Long patientGender) { this.patientGender = patientGender; }
    public Long getPatientGender() { return patientGender; }
    public void setPatientAge(Long patientAge) { this.patientAge = patientAge; }
    public Long getPatientAge() { return patientAge; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public String getServiceType() { return serviceType; }
    public void setDepartment(String department) { this.department = department; }
    public String getDepartment() { return department; }
    public void setDiagnosisCode(String diagnosisCode) { this.diagnosisCode = diagnosisCode; }
    public String getDiagnosisCode() { return diagnosisCode; }
    public void setDiagnosisName(String diagnosisName) { this.diagnosisName = diagnosisName; }
    public String getDiagnosisName() { return diagnosisName; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public Long getDoctorId() { return doctorId; }
    public void setServiceDate(Date serviceDate) { this.serviceDate = serviceDate; }
    public Date getServiceDate() { return serviceDate; }
    public void setDischargeDate(Date dischargeDate) { this.dischargeDate = dischargeDate; }
    public Date getDischargeDate() { return dischargeDate; }
    public void setDaysInHospital(Long daysInHospital) { this.daysInHospital = daysInHospital; }
    public Long getDaysInHospital() { return daysInHospital; }
    public void setServiceStatus(Long serviceStatus) { this.serviceStatus = serviceStatus; }
    public Long getServiceStatus() { return serviceStatus; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getCreatedAt() { return createdAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    public Date getUpdatedAt() { return updatedAt; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("serviceCode", getServiceCode())
            .append("orgId", getOrgId())
            .append("orgName", getOrgName())
            .append("serviceCategory", getServiceCategory())
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
