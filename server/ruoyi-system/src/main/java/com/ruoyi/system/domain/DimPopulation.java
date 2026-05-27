package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * ?????对象 dim_population
 * 
 * @author ruoyi
 * @date 2026-05-27
 */
public class DimPopulation extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long id;

    /** ?? ID */
    @Excel(name = "?? ID")
    private Long regionId;

    /** ??? */
    @Excel(name = "???")
    private Long totalPopulation;

    /** ???? */
    @Excel(name = "????")
    private Long malePopulation;

    /** ???? */
    @Excel(name = "????")
    private Long femalePopulation;

    /** 0-14??? */
    @Excel(name = "0-14???")
    private Long age014;

    /** 15-59??? */
    @Excel(name = "15-59???")
    private Long age1559;

    /** 60????? */
    @Excel(name = "60?????")
    private Long age60Plus;

    /** ???? */
    @Excel(name = "????")
    private Long statYear;

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

    public void setRegionId(Long regionId) 
    {
        this.regionId = regionId;
    }

    public Long getRegionId() 
    {
        return regionId;
    }

    public void setTotalPopulation(Long totalPopulation) 
    {
        this.totalPopulation = totalPopulation;
    }

    public Long getTotalPopulation() 
    {
        return totalPopulation;
    }

    public void setMalePopulation(Long malePopulation) 
    {
        this.malePopulation = malePopulation;
    }

    public Long getMalePopulation() 
    {
        return malePopulation;
    }

    public void setFemalePopulation(Long femalePopulation) 
    {
        this.femalePopulation = femalePopulation;
    }

    public Long getFemalePopulation() 
    {
        return femalePopulation;
    }

    public void setAge014(Long age014) 
    {
        this.age014 = age014;
    }

    public Long getAge014() 
    {
        return age014;
    }

    public void setAge1559(Long age1559) 
    {
        this.age1559 = age1559;
    }

    public Long getAge1559() 
    {
        return age1559;
    }

    public void setAge60Plus(Long age60Plus) 
    {
        this.age60Plus = age60Plus;
    }

    public Long getAge60Plus() 
    {
        return age60Plus;
    }

    public void setStatYear(Long statYear) 
    {
        this.statYear = statYear;
    }

    public Long getStatYear() 
    {
        return statYear;
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
            .append("regionId", getRegionId())
            .append("totalPopulation", getTotalPopulation())
            .append("malePopulation", getMalePopulation())
            .append("femalePopulation", getFemalePopulation())
            .append("age014", getAge014())
            .append("age1559", getAge1559())
            .append("age60Plus", getAge60Plus())
            .append("statYear", getStatYear())
            .append("createdAt", getCreatedAt())
            .toString();
    }
}
