package com.dayflow.hrmtool.leave.dto;

import lombok.Data;

@Data
public class LeaveTypeDto {
    private Long id;
    private Long companyId;
    private String name;
    private boolean requiresAttachment;
    private boolean isPaid;
}
