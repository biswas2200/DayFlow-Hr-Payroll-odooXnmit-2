package com.dayflow.hrmtool.employee.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class PrivateInfoDto {
    private LocalDate dateOfBirth;
    private String residingAddress;
    private String nationality;
    private String personalEmail;
    private String gender;
    private String maritalStatus;
    private LocalDate dateOfJoining;
    private BankDetailDto bankDetails;
}
