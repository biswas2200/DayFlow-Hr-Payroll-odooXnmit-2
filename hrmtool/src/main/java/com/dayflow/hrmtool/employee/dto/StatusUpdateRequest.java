package com.dayflow.hrmtool.employee.dto;

import com.dayflow.hrmtool.employee.EmployeeStatus;
import lombok.Data;

@Data
public class StatusUpdateRequest {
    private EmployeeStatus status;
}
