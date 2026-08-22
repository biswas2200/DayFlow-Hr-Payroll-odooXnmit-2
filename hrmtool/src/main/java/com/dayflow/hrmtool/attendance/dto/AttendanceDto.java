package com.dayflow.hrmtool.attendance.dto;

import com.dayflow.hrmtool.attendance.AttendanceStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class AttendanceDto {
    private Long id;
    private LocalDate date;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private String workHours;
    private Long extraHours;
    private AttendanceStatus status;
}
