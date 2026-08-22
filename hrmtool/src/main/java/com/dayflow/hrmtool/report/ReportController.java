package com.dayflow.hrmtool.report;

import com.dayflow.hrmtool.report.dto.DashboardSummaryDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/dashboard")
    public DashboardSummaryDto getDashboard(@RequestParam Long companyId) {
        return reportService.getDashboard(companyId);
    }

    @GetMapping("/attendance-summary")
    public Object getAttendanceSummary(@RequestParam(defaultValue = "DAILY") String period) {
        return reportService.getAttendanceSummary(period);
    }

    @GetMapping("/leave-utilization")
    public Object getLeaveUtilization() {
        return reportService.getLeaveUtilization();
    }
}
