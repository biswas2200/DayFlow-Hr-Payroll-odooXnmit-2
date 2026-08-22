package com.dayflow.hrmtool.leave.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ApplyLeaveDto {
    private Long employeeId;
    private Long leaveTypeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal numDays;
    private String attachmentUrl;
}
