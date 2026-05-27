package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * ???????对象 medical_institution
 * 
 * @author ruoyi
 * @date 2026-05-27
 */
public class MedicalInstitution extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ?? */
    private Long id;

    /** ???? */
    @Excel(name = "????")
    private String orgCode;

    /** ???? */
    @Excel(name = "????")
    private String orgName;

    /** ???? */
    @Excel(name = "????")
    private String orgType;

    /** ?? */
    @Excel(name = "??")
    private String orgLevel;

    /** ???? ID */
    @Excel(name = "???? ID")
    private Long regionId;

    /** ???? */
    @Excel(name = "????")
    private String address;

    /** ???? */
    @Excel(name = "????")
    private String contactPhone;

    /** ?????1??? 0??? */
    @Excel(name = "?????1??? 0???")
    private Long isActive;

    /** ???? */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "????", width = 30, dateFormat = "yyyy-MM-dd")
    private Date establishedDate;

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

    public void setOrgCode(String orgCode) 
    {
        this.orgCode = orgCode;
    }

    public String getOrgCode() 
    {
        return orgCode;
    }

    public void setOrgName(String orgName) 
    {
        this.orgName = orgName;
    }

    public String getOrgName() 
    {
        return orgName;
    }

    public void setOrgType(String orgType) 
    {
        this.orgType = orgType;
    }

    public String getOrgType() 
    {
        return orgType;
    }

    public void setOrgLevel(String orgLevel) 
    {
        this.orgLevel = orgLevel;
    }

    public String getOrgLevel() 
    {
        return orgLevel;
    }

    public void setRegionId(Long regionId) 
    {
        this.regionId = regionId;
    }

    public Long getRegionId() 
    {
        return regionId;
    }

    public void setAddress(String address) 
    {
        this.address = address;
    }

    public String getAddress() 
    {
        return address;
    }

    public void setContactPhone(String contactPhone) 
    {
        this.contactPhone = contactPhone;
    }

    public String getContactPhone() 
    {
        return contactPhone;
    }

    public void setIsActive(Long isActive) 
    {
        this.isActive = isActive;
    }

    public Long getIsActive() 
    {
        return isActive;
    }

    public void setEstablishedDate(Date establishedDate) 
    {
        this.establishedDate = establishedDate;
    }

    public Date getEstablishedDate() 
    {
        return establishedDate;
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
            .append("orgCode", getOrgCode())
            .append("orgName", getOrgName())
            .append("orgType", getOrgType())
            .append("orgLevel", getOrgLevel())
            .append("regionId", getRegionId())
            .append("address", getAddress())
            .append("contactPhone", getContactPhone())
            .append("isActive", getIsActive())
            .append("establishedDate", getEstablishedDate())
            .append("createdAt", getCreatedAt())
            .append("updatedAt", getUpdatedAt())
            .toString();
    }
}
