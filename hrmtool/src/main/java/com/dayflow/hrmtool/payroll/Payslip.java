package com.dayflow.hrmtool.payroll;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class Payslip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long employeeId;
    private Integer month;
    private Integer year;
    private Long generatedBy;
    private LocalDateTime generatedAt;
    
    private Double payableDays;
    private Double grossSalary;
    private Double pfEmployee;
    private Double professionalTax;
    private Double netSalary;
    private String pdfPath;

    public Double getPayableDays() { return payableDays; }
    public void setPayableDays(Double payableDays) { this.payableDays = payableDays; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public Long getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(Long generatedBy) { this.generatedBy = generatedBy; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    public Double getGrossSalary() { return grossSalary; }
    public void setGrossSalary(Double grossSalary) { this.grossSalary = grossSalary; }
    public Double getPfEmployee() { return pfEmployee; }
    public void setPfEmployee(Double pfEmployee) { this.pfEmployee = pfEmployee; }
    public Double getProfessionalTax() { return professionalTax; }
    public void setProfessionalTax(Double professionalTax) { this.professionalTax = professionalTax; }
    public Double getNetSalary() { return netSalary; }
    public void setNetSalary(Double netSalary) { this.netSalary = netSalary; }
    public String getPdfPath() { return pdfPath; }
    public void setPdfPath(String pdfPath) { this.pdfPath = pdfPath; }
}
