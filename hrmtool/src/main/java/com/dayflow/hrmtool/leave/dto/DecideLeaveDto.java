package com.dayflow.hrmtool.leave.dto;

import com.dayflow.hrmtool.leave.LeaveStatus;
import lombok.Data;

@Data
public class DecideLeaveDto {
    private Long requestId;
    private Long approverId;
    private LeaveStatus status;
    private String approverComment;
}
