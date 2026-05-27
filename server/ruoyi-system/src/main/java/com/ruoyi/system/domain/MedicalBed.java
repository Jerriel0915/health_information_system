package com.ruoyi.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * ???????对象 medical_bed
 * 
 * @author ruoyi
 * @date 2026-05-27
 */
public class MedicalBed extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ?? */
    private Long id;

    /** ???? ID */
    @Excel(name = "???? ID")
    private Long orgId;

    /** ????? */
    @Excel(name = "?????")
    private Long bedCount;

    /** ????? */
    @Excel(name = "?????")
    private Long actualBedCount;

    /** ????? */
    @Excel(name = "?????")
    private Long usedBedCount;

    /** ??????%%? */
    @Excel(name = "??????%%?")
    private BigDecimal bedUsageRate;

    /** ???? */
    @Excel(name = "????")
    private String deptType;

    /** ???? */
    @Excel(name = "????")
    private Long statYear;

    /** ???? */
    @Excel(name = "????")
    private Long statMonth;

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

    public void setOrgId(Long orgId) 
    {
        this.orgId = orgId;
    }

    public Long getOrgId() 
    {
        return orgId;
    }

    public void setBedCount(Long bedCount) 
    {
        this.bedCount = bedCount;
    }

    public Long getBedCount() 
    {
        return bedCount;
    }

    public void setActualBedCount(Long actualBedCount) 
    {
        this.actualBedCount = actualBedCount;
    }

    public Long getActualBedCount() 
    {
        return actualBedCount;
    }

    public void setUsedBedCount(Long usedBedCount) 
    {
        this.usedBedCount = usedBedCount;
    }

    public Long getUsedBedCount() 
    {
        return usedBedCount;
    }

    public void setBedUsageRate(BigDecimal bedUsageRate) 
    {
        this.bedUsageRate = bedUsageRate;
    }

    public BigDecimal getBedUsageRate() 
    {
        return bedUsageRate;
    }

    public void setDeptType(String deptType) 
    {
        this.deptType = deptType;
    }

    public String getDeptType() 
    {
        return deptType;
    }

    public void setStatYear(Long statYear) 
    {
        this.statYear = statYear;
    }

    public Long getStatYear() 
    {
        return statYear;
    }

    public void setStatMonth(Long statMonth) 
    {
        this.statMonth = statMonth;
    }

    public Long getStatMonth() 
    {
        return statMonth;
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
            .append("orgId", getOrgId())
            .append("bedCount", getBedCount())
            .append("actualBedCount", getActualBedCount())
            .append("usedBedCount", getUsedBedCount())
            .append("bedUsageRate", getBedUsageRate())
            .append("deptType", getDeptType())
            .append("statYear", getStatYear())
            .append("statMonth", getStatMonth())
            .append("createdAt", getCreatedAt())
            .append("updatedAt", getUpdatedAt())
            .toString();
    }
}
