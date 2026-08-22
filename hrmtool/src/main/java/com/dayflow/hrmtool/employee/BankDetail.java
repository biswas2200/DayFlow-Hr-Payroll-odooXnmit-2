package com.dayflow.hrmtool.employee;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "bank_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankDetail {

    @Id
    @Column(name = "employee_id")
    private Long employeeId;

    private String accountNumber;
    private String bankName;
    private String ifscCode;
    private String panNo;
    private String uanNo;
    private String empCode;
}
