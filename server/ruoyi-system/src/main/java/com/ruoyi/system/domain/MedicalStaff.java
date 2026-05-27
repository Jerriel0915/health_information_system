package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * ???????对象 medical_staff
 * 
 * @author ruoyi
 * @date 2026-05-27
 */
public class MedicalStaff extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ?? */
    private Long id;

    /** ???? */
    @Excel(name = "????")
    private String staffCode;

    /** ?? */
    @Excel(name = "??")
    private String staffName;

    /** ???1? 2? */
    @Excel(name = "???1? 2?")
    private Long gender;

    /** ???? */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "????", width = 30, dateFormat = "yyyy-MM-dd")
    private Date birthDate;

    /** ???? ID */
    @Excel(name = "???? ID")
    private Long orgId;

    /** ?? */
    @Excel(name = "??")
    private String department;

    /** ?? */
    @Excel(name = "??")
    private String jobTitle;

    /** ???? */
    @Excel(name = "????")
    private String jobCategory;

    /** ?? */
    @Excel(name = "??")
    private String education;

    /** ???? */
    @Excel(name = "????")
    private Long isActive;

    /** ???? */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "????", width = 30, dateFormat = "yyyy-MM-dd")
    private Date entryDate;

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

    public void setStaffCode(String staffCode) 
    {
        this.staffCode = staffCode;
    }

    public String getStaffCode() 
    {
        return staffCode;
    }

    public void setStaffName(String staffName) 
    {
        this.staffName = staffName;
    }

    public String getStaffName() 
    {
        return staffName;
    }

    public void setGender(Long gender) 
    {
        this.gender = gender;
    }

    public Long getGender() 
    {
        return gender;
    }

    public void setBirthDate(Date birthDate) 
    {
        this.birthDate = birthDate;
    }

    public Date getBirthDate() 
    {
        return birthDate;
    }

    public void setOrgId(Long orgId) 
    {
        this.orgId = orgId;
    }

    public Long getOrgId() 
    {
        return orgId;
    }

    public void setDepartment(String department) 
    {
        this.department = department;
    }

    public String getDepartment() 
    {
        return department;
    }

    public void setJobTitle(String jobTitle) 
    {
        this.jobTitle = jobTitle;
    }

    public String getJobTitle() 
    {
        return jobTitle;
    }

    public void setJobCategory(String jobCategory) 
    {
        this.jobCategory = jobCategory;
    }

    public String getJobCategory() 
    {
        return jobCategory;
    }

    public void setEducation(String education) 
    {
        this.education = education;
    }

    public String getEducation() 
    {
        return education;
    }

    public void setIsActive(Long isActive) 
    {
        this.isActive = isActive;
    }

    public Long getIsActive() 
    {
        return isActive;
    }

    public void setEntryDate(Date entryDate) 
    {
        this.entryDate = entryDate;
    }

    public Date getEntryDate() 
    {
        return entryDate;
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
            .append("staffCode", getStaffCode())
            .append("staffName", getStaffName())
            .append("gender", getGender())
            .append("birthDate", getBirthDate())
            .append("orgId", getOrgId())
            .append("department", getDepartment())
            .append("jobTitle", getJobTitle())
            .append("jobCategory", getJobCategory())
            .append("education", getEducation())
            .append("isActive", getIsActive())
            .append("entryDate", getEntryDate())
            .append("createdAt", getCreatedAt())
            .append("updatedAt", getUpdatedAt())
            .toString();
    }
}
