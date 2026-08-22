package com.dayflow.hrmtool.employee.dto;

import lombok.Data;

@Data
public class EmployeeSelfEditRequest {
    private String phone;
    private String residingAddress;
    private String profilePictureUrl;
    private String personalEmail;
}
