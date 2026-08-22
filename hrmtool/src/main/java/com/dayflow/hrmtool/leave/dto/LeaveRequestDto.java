package com.dayflow.hrmtool.leave.dto;

import com.dayflow.hrmtool.leave.LeaveStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;

@Data
public class LeaveRequestDto {
    private Long id;
    private Long employeeId;
    private Long leaveTypeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal numDays;
    private String attachmentUrl;
    private LeaveStatus status;
    private Long approverId;
    private String approverComment;
    private Instant decidedAt;
}
