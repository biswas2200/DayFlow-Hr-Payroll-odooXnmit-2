package com.dayflow.hrmtool.payroll.dto;

import lombok.Data;

/**
 * Request body for POST /api/v1/payslips/generate.
 * Matches the frontend PayrollService.generatePayslip() which sends:
 * { employeeId, month, year }
 */
@Data
public class GeneratePayslipRequest {
    private Long employeeId;
    private int month;
    private int year;
}
