package com.dayflow.hrmtool.leave.dto;

import lombok.Data;
import java.util.List;

@Data
public class LeaveCalendarDto {
    private List<LeaveRequestDto> requests;
    private List<PublicHolidayDto> holidays;
}
