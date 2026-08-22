package com.dayflow.hrmtool.report;

import com.dayflow.hrmtool.auth.AppUser;
import com.dayflow.hrmtool.common.ResourceNotFoundException;
import com.dayflow.hrmtool.employee.EmployeeRepository;
import com.dayflow.hrmtool.report.dto.DashboardSummaryDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;
    private final EmployeeRepository employeeRepository;

    public ReportController(ReportService reportService, EmployeeRepository employeeRepository) {
        this.reportService = reportService;
        this.employeeRepository = employeeRepository;
    }

    private Long resolveCompanyId(AppUser currentUser) {
        Long empId = currentUser.getEmployeeId();
        if (empId == null) {
            throw new ResourceNotFoundException("Admin has no associated Employee profile");
        }
        return employeeRepository.findById(empId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found"))
                .getCompanyId();
    }

    @GetMapping("/dashboard")
    public DashboardSummaryDto getDashboard(@AuthenticationPrincipal AppUser currentUser) {
        return reportService.getDashboard(resolveCompanyId(currentUser));
    }

    @GetMapping("/attendance-summary")
    public java.util.Map<String, Object> getAttendanceSummary(@RequestParam(defaultValue = "DAILY") String period) {
        return java.util.Collections.emptyMap();
    }

    @GetMapping("/leave-utilization")
    public java.util.Map<String, Object> getLeaveUtilization() {
        return java.util.Collections.emptyMap();
    }
}
