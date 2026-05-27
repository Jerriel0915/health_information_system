package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * ???????对象 dim_region
 * 
 * @author ruoyi
 * @date 2026-05-27
 */
public class DimRegion extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ?? */
    private Long id;

    /** ?????? */
    @Excel(name = "??????")
    private String regionCode;

    /** ?? */
    @Excel(name = "??")
    private String regionName;

    /** ???1? 2? 3?? */
    @Excel(name = "???1? 2? 3??")
    private Long regionLevel;

    /** ?????? ID */
    @Excel(name = "?????? ID")
    private Long parentId;

    /** ?? */
    @Excel(name = "??")
    private Long sortOrder;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private Date createdAt;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setRegionCode(String regionCode) 
    {
        this.regionCode = regionCode;
    }

    public String getRegionCode() 
    {
        return regionCode;
    }

    public void setRegionName(String regionName) 
    {
        this.regionName = regionName;
    }

    public String getRegionName() 
    {
        return regionName;
    }

    public void setRegionLevel(Long regionLevel) 
    {
        this.regionLevel = regionLevel;
    }

    public Long getRegionLevel() 
    {
        return regionLevel;
    }

    public void setParentId(Long parentId) 
    {
        this.parentId = parentId;
    }

    public Long getParentId() 
    {
        return parentId;
    }

    public void setSortOrder(Long sortOrder) 
    {
        this.sortOrder = sortOrder;
    }

    public Long getSortOrder() 
    {
        return sortOrder;
    }

    public void setCreatedAt(Date createdAt) 
    {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() 
    {
        return createdAt;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("regionCode", getRegionCode())
            .append("regionName", getRegionName())
            .append("regionLevel", getRegionLevel())
            .append("parentId", getParentId())
            .append("sortOrder", getSortOrder())
            .append("createdAt", getCreatedAt())
            .toString();
    }
}
