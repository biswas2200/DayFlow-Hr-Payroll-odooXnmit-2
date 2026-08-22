package com.dayflow.hrmtool.leave.event;

import com.dayflow.hrmtool.leave.LeaveStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveDecidedEvent {
    private Long leaveRequestId;
    private Long employeeId;
    private Long approverId;
    private LeaveStatus status;
}
