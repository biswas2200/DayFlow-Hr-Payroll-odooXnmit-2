package com.dayflow.hrmtool.company.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCompanyRequest {
    @NotBlank(message = "Company name is required")
    private String name;

    private String logoUrl;

    @NotBlank(message = "Company initials are required")
    private String initials;

    @Builder.Default
    private int workingDaysPerWeek = 5;

    private BigDecimal breakHours;
}
