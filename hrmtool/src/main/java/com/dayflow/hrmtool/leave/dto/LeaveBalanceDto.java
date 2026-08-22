package com.dayflow.hrmtool.leave.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class LeaveBalanceDto {
    private Long leaveTypeId;
    private String leaveTypeName;
    private BigDecimal allocatedDays;
    private BigDecimal usedDays;
    private BigDecimal availableDays;
}
