package com.dayflow.hrmtool.payroll;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class SalaryStructure {
    @Id
    private Long employeeId;
    private Double monthlyWage;
    private Double yearlyWage;
    private Double pfEmployeePercent;
    private Double pfEmployerPercent;
    private Double professionalTax;
    private Integer workingDaysPerWeek;
    private Double breakHours;
    private LocalDateTime updatedAt;
    private Long updatedBy;

    // Getters and Setters
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public Double getMonthlyWage() { return monthlyWage; }
    public void setMonthlyWage(Double monthlyWage) { this.monthlyWage = monthlyWage; }
    public Double getYearlyWage() { return yearlyWage; }
    public void setYearlyWage(Double yearlyWage) { this.yearlyWage = yearlyWage; }
    public Double getPfEmployeePercent() { return pfEmployeePercent; }
    public void setPfEmployeePercent(Double pfEmployeePercent) { this.pfEmployeePercent = pfEmployeePercent; }
    public Double getPfEmployerPercent() { return pfEmployerPercent; }
    public void setPfEmployerPercent(Double pfEmployerPercent) { this.pfEmployerPercent = pfEmployerPercent; }
    public Double getProfessionalTax() { return professionalTax; }
    public void setProfessionalTax(Double professionalTax) { this.professionalTax = professionalTax; }
    public Integer getWorkingDaysPerWeek() { return workingDaysPerWeek; }
    public void setWorkingDaysPerWeek(Integer workingDaysPerWeek) { this.workingDaysPerWeek = workingDaysPerWeek; }
    public Double getBreakHours() { return breakHours; }
    public void setBreakHours(Double breakHours) { this.breakHours = breakHours; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Long getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
}
