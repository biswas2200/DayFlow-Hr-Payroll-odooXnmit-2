package com.dayflow.hrmtool.attendance.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttendanceSummaryDto {
    private double daysPresent;
    private int leavesCount;
    private int totalWorkingDays;
    private int month;
    private int year;
}
