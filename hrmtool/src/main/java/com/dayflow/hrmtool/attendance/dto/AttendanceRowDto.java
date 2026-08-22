package com.dayflow.hrmtool.attendance.dto;

import com.dayflow.hrmtool.attendance.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Flat DTO combining attendance info + employee info for Admin views.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRowDto {
    private Long id;
    private LocalDate date;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private String workHours;
    private String extraHours;
    private AttendanceStatus status;
    private Long employeeId;
    private String employeeName;
}
