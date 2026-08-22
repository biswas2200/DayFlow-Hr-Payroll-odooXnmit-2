package com.dayflow.hrmtool.leave.dto;

import lombok.Data;
import java.util.List;

@Data
public class LeaveCalendarDto {
    private int year;
    private List<LeaveRequestDto> requests;
    private List<PublicHolidayDto> publicHolidays;
}
