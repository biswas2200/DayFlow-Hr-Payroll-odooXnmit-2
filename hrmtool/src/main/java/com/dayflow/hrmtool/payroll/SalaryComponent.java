package com.dayflow.hrmtool.payroll;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
public class SalaryComponent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long salaryStructureId;
    
    @Enumerated(EnumType.STRING)
    private ComponentType type;
    
    @Enumerated(EnumType.STRING)
    private ComputationType computationType;
    
    private Double value;
    private Double computedAmount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSalaryStructureId() { return salaryStructureId; }
    public void setSalaryStructureId(Long salaryStructureId) { this.salaryStructureId = salaryStructureId; }
    public ComponentType getType() { return type; }
    public void setType(ComponentType type) { this.type = type; }
    public ComputationType getComputationType() { return computationType; }
    public void setComputationType(ComputationType computationType) { this.computationType = computationType; }
    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
    public Double getComputedAmount() { return computedAmount; }
    public void setComputedAmount(Double computedAmount) { this.computedAmount = computedAmount; }
}
