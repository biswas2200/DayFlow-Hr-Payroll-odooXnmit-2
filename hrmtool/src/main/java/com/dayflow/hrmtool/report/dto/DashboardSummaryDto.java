package com.dayflow.hrmtool.report.dto;

import java.util.Map;

public class DashboardSummaryDto {
    private Double attendancePercentToday;
    private Map<String, Long> leaveTrends;
    private Map<String, Long> headcountByDepartment;

    public DashboardSummaryDto() {}

    public Double getAttendancePercentToday() { return attendancePercentToday; }
    public void setAttendancePercentToday(Double attendancePercentToday) { this.attendancePercentToday = attendancePercentToday; }

    public Map<String, Long> getLeaveTrends() { return leaveTrends; }
    public void setLeaveTrends(Map<String, Long> leaveTrends) { this.leaveTrends = leaveTrends; }

    public Map<String, Long> getHeadcountByDepartment() { return headcountByDepartment; }
    public void setHeadcountByDepartment(Map<String, Long> headcountByDepartment) { this.headcountByDepartment = headcountByDepartment; }
}
