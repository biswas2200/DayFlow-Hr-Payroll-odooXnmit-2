package com.dayflow.hrmtool.employee.dto;

import com.dayflow.hrmtool.auth.Role;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateEmployeeRequest {
    private String companyName;
    private String companyLogoUrl;

    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @NotBlank
    private String email;
    @NotBlank
    private String phone;
    @NotBlank
    private String password;
    @NotBlank
    private String confirmPassword;
    
    private String jobPosition;
    private String department;
    private Role role = Role.EMPLOYEE;
}
