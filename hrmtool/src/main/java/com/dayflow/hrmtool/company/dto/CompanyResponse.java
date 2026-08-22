package com.dayflow.hrmtool.company.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponse {
    private Long id;
    private String name;
    private String logoUrl;
    private String initials;
    private int workingDaysPerWeek;
    private BigDecimal breakHours;
    private Instant createdAt;
    private Instant updatedAt;
}
