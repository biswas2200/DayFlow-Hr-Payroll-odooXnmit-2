package com.dayflow.hrmtool.employee.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeProfileDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String workEmail;
    private String phone;
    private String jobPosition;
    private String department;
    private String profilePictureUrl;
    
    private ResumeDto resume;
    private PrivateInfoDto privateInfo;
    
    private boolean salaryVisible;
    private boolean salaryEditable;
}
