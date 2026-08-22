package com.dayflow.hrmtool.leave.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LeaveTypeDto {
    private Long id;
    private Long companyId;
    private String name;
    private boolean requiresAttachment;

    @JsonProperty("isPaid")
    private boolean isPaid;
}
