package com.dayflow.hrmtool.leave.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class LeaveAllocationDto {
    private Long id;
    private Long employeeId;
    private Long leaveTypeId;
    private int year;
    private BigDecimal allocatedDays;
    private BigDecimal usedDays;
}
