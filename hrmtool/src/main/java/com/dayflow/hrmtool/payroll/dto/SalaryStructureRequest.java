package com.dayflow.hrmtool.payroll.dto;

import com.dayflow.hrmtool.payroll.SalaryComponent;
import lombok.Data;

import java.util.List;

@Data
public class SalaryStructureRequest {
    private Double monthlyWage;
    private Integer workingDaysPerWeek;
    private Double breakHours;
    private List<SalaryComponent> components;
    private Double pfEmployeePercent;
    private Double pfEmployerPercent;
    private Double professionalTax;
}
