package com.dayflow.hrmtool.employee.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PrivateInfoDto {
    private String personalEmail;
    private String phone;
    private String residingAddress;
    private BankDetailDto bankDetails;
}
