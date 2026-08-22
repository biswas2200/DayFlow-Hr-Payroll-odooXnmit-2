package com.dayflow.hrmtool.leave.dto;

import lombok.Data;

/**
 * Request body for leave approve/reject endpoints.
 */
@Data
public class ApproveRejectRequest {
    private String comment;
}
