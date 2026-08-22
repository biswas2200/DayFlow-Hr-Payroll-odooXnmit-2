package com.dayflow.hrmtool.payroll.dto;

import com.dayflow.hrmtool.payroll.SalaryComponent;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SalaryStructureDto {
    private Long employeeId;
    private Double monthlyWage;
    private Double yearlyWage;
    private Integer workingDaysPerWeek;
    private Double breakHours;
    private List<SalaryComponent> components;
    private Double pfEmployeePercent;
    private Double pfEmployerPercent;
    private Double professionalTax;
}
