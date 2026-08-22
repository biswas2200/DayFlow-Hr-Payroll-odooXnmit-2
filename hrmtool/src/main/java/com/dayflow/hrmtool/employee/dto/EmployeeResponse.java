package com.dayflow.hrmtool.employee.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeResponse {
    private Long id;
    private String loginId;
    private boolean tempPasswordIssued;
    private String tempPassword;
}
