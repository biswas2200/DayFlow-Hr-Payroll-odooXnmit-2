package com.dayflow.hrmtool.employee.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BankDetailDto {
    private String bankName;
    private String accountNumber;
    private String ifscCode;
    private String panNo;
    private String uanNo;
    private String empCode;
}
