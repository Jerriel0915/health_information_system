package com.ruoyi.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * ?????对象 medical_cost
 * 
 * @author ruoyi
 * @date 2026-05-27
 */
public class MedicalCost extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ?? */
    private Long id;

    /** ?????? ID */
    @Excel(name = "?????? ID")
    private Long serviceId;

    /** ??? */
    @Excel(name = "???")
    private BigDecimal totalCost;

    /** ??? */
    @Excel(name = "???")
    private BigDecimal drugCost;

    /** ??? */
    @Excel(name = "???")
    private BigDecimal treatmentCost;

    /** ??? */
    @Excel(name = "???")
    private BigDecimal surgeryCost;

    /** ??? */
    @Excel(name = "???")
    private BigDecimal inspectionCost;

    /** ??? */
    @Excel(name = "???")
    private BigDecimal laboratoryCost;

    /** ??? */
    @Excel(name = "???")
    private BigDecimal bedCost;

    /** ??? */
    @Excel(name = "???")
    private BigDecimal nursingCost;

    /** ???? */
    @Excel(name = "????")
    private BigDecimal otherCost;

    /** ?????? */
    @Excel(name = "??????")
    private BigDecimal insurancePaid;

    /** ???? */
    @Excel(name = "????")
    private BigDecimal selfPaid;

    /** ???? */
    @Excel(name = "????")
    private String costCategory;

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

    public void setServiceId(Long serviceId) 
    {
        this.serviceId = serviceId;
    }

    public Long getServiceId() 
    {
        return serviceId;
    }

    public void setTotalCost(BigDecimal totalCost) 
    {
        this.totalCost = totalCost;
    }

    public BigDecimal getTotalCost() 
    {
        return totalCost;
    }

    public void setDrugCost(BigDecimal drugCost) 
    {
        this.drugCost = drugCost;
    }

    public BigDecimal getDrugCost() 
    {
        return drugCost;
    }

    public void setTreatmentCost(BigDecimal treatmentCost) 
    {
        this.treatmentCost = treatmentCost;
    }

    public BigDecimal getTreatmentCost() 
    {
        return treatmentCost;
    }

    public void setSurgeryCost(BigDecimal surgeryCost) 
    {
        this.surgeryCost = surgeryCost;
    }

    public BigDecimal getSurgeryCost() 
    {
        return surgeryCost;
    }

    public void setInspectionCost(BigDecimal inspectionCost) 
    {
        this.inspectionCost = inspectionCost;
    }

    public BigDecimal getInspectionCost() 
    {
        return inspectionCost;
    }

    public void setLaboratoryCost(BigDecimal laboratoryCost) 
    {
        this.laboratoryCost = laboratoryCost;
    }

    public BigDecimal getLaboratoryCost() 
    {
        return laboratoryCost;
    }

    public void setBedCost(BigDecimal bedCost) 
    {
        this.bedCost = bedCost;
    }

    public BigDecimal getBedCost() 
    {
        return bedCost;
    }

    public void setNursingCost(BigDecimal nursingCost) 
    {
        this.nursingCost = nursingCost;
    }

    public BigDecimal getNursingCost() 
    {
        return nursingCost;
    }

    public void setOtherCost(BigDecimal otherCost) 
    {
        this.otherCost = otherCost;
    }

    public BigDecimal getOtherCost() 
    {
        return otherCost;
    }

    public void setInsurancePaid(BigDecimal insurancePaid) 
    {
        this.insurancePaid = insurancePaid;
    }

    public BigDecimal getInsurancePaid() 
    {
        return insurancePaid;
    }

    public void setSelfPaid(BigDecimal selfPaid) 
    {
        this.selfPaid = selfPaid;
    }

    public BigDecimal getSelfPaid() 
    {
        return selfPaid;
    }

    public void setCostCategory(String costCategory) 
    {
        this.costCategory = costCategory;
    }

    public String getCostCategory() 
    {
        return costCategory;
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
            .append("serviceId", getServiceId())
            .append("totalCost", getTotalCost())
            .append("drugCost", getDrugCost())
            .append("treatmentCost", getTreatmentCost())
            .append("surgeryCost", getSurgeryCost())
            .append("inspectionCost", getInspectionCost())
            .append("laboratoryCost", getLaboratoryCost())
            .append("bedCost", getBedCost())
            .append("nursingCost", getNursingCost())
            .append("otherCost", getOtherCost())
            .append("insurancePaid", getInsurancePaid())
            .append("selfPaid", getSelfPaid())
            .append("costCategory", getCostCategory())
            .append("createdAt", getCreatedAt())
            .toString();
    }
}
