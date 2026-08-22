package com.dayflow.hrmtool.employee.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class CertificationDto {
    private Long id;
    private String name;
    private String issuer;
    private LocalDate issueDate;
}
