package com.dayflow.hrmtool.employee.dto;

import com.dayflow.hrmtool.employee.EmployeeStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class EmployeeAdminEditRequest extends EmployeeSelfEditRequest {
    private String jobPosition;
    private String department;
    private Long managerId;
    private EmployeeStatus status;
}
