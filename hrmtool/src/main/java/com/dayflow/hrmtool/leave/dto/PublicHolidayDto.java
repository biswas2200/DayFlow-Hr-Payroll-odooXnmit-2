package com.dayflow.hrmtool.leave.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PublicHolidayDto {
    private Long id;
    private LocalDate date;
    private String name;
}
