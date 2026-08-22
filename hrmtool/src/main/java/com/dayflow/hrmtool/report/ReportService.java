package com.dayflow.hrmtool.report;

import com.dayflow.hrmtool.report.dto.DashboardSummaryDto;
import com.dayflow.hrmtool.employee.EmployeeRepository;
import com.dayflow.hrmtool.employee.EmployeeStatus;
import com.dayflow.hrmtool.attendance.AttendanceRepository;
import com.dayflow.hrmtool.leave.LeaveRequestRepository;
import com.dayflow.hrmtool.employee.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;

    public DashboardSummaryDto getDashboard(Long companyId) {
        DashboardSummaryDto summary = new DashboardSummaryDto();
        
        List<Employee> employees = employeeRepository.findByCompanyId(companyId);
        long activeEmployees = employees.stream().filter(e -> e.getStatus() == EmployeeStatus.ACTIVE).count();
        
        LocalDate today = LocalDate.now();
        List<Long> employeeIds = employees.stream().map(Employee::getId).collect(Collectors.toList());
        
        long presentToday = 0;
        if (!employeeIds.isEmpty()) {
            presentToday = attendanceRepository.findByDateAndEmployeeIdIn(today, employeeIds).size();
        }
        
        double attendancePercent = activeEmployees > 0 ? (double) presentToday / activeEmployees * 100 : 0.0;
        summary.setAttendancePercentToday(Math.round(attendancePercent * 100.0) / 100.0);
        
        Map<String, Long> headcount = employees.stream()
            .filter(e -> e.getDepartment() != null)
            .collect(Collectors.groupingBy(Employee::getDepartment, Collectors.counting()));
        summary.setHeadcountByDepartment(headcount);
        
        summary.setLeaveTrends(new HashMap<>()); // Placeholder for leave trends
        
        return summary;
    }
    
    public Object getAttendanceSummary(String period) {
        return new Object();
    }
    
    public Object getLeaveUtilization() {
        return new Object();
    }
}
