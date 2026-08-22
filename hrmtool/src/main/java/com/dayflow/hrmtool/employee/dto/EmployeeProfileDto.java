package com.dayflow.hrmtool.employee.dto;

import com.dayflow.hrmtool.employee.EmployeeStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeProfileDto {
    private Long id;
    private String loginId;
    private String firstName;
    private String lastName;
    private String jobPosition;
    private String department;
    private String manager;
    private String location;
    private String email;
    private String mobile;
    private String profilePictureUrl;
    private EmployeeStatus status;

    private ResumeDto resume;
    private PrivateInfoDto privateInfo;

    private boolean salaryVisible;
    private boolean salaryEditable;
}
