package com.dayflow.hrmtool.report.dto;

import java.util.List;

public class DashboardSummaryDto {
    private Double attendancePercentToday;
    private List<MonthCount> leaveTrends;
    private List<DepartmentCount> headcountByDepartment;

    public DashboardSummaryDto() {}

    public Double getAttendancePercentToday() { return attendancePercentToday; }
    public void setAttendancePercentToday(Double attendancePercentToday) { this.attendancePercentToday = attendancePercentToday; }

    public List<MonthCount> getLeaveTrends() { return leaveTrends; }
    public void setLeaveTrends(List<MonthCount> leaveTrends) { this.leaveTrends = leaveTrends; }

    public List<DepartmentCount> getHeadcountByDepartment() { return headcountByDepartment; }
    public void setHeadcountByDepartment(List<DepartmentCount> headcountByDepartment) { this.headcountByDepartment = headcountByDepartment; }

    public static class MonthCount {
        private String month;
        private Long count;

        public MonthCount() {}
        public MonthCount(String month, Long count) {
            this.month = month;
            this.count = count;
        }

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }
        public Long getCount() { return count; }
        public void setCount(Long count) { this.count = count; }
    }

    public static class DepartmentCount {
        private String department;
        private Long count;

        public DepartmentCount() {}
        public DepartmentCount(String department, Long count) {
            this.department = department;
            this.count = count;
        }

        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public Long getCount() { return count; }
        public void setCount(Long count) { this.count = count; }
    }
}
