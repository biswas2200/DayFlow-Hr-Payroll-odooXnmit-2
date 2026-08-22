package com.dayflow.hrmtool.payroll.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class GeneratePayslipDto {
    private Long employeeId;
    private LocalDate payPeriodStart;
    private LocalDate payPeriodEnd;
}
