package com.dayflow.hrmtool.employee.dto;

import com.dayflow.hrmtool.employee.StatusDot;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeCardDto {
    private Long id;
    private String name;
    private String profilePictureUrl;
    private StatusDot statusDot;
}
