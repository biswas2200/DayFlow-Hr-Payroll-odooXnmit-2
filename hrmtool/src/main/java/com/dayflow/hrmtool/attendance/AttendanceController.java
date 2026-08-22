package com.dayflow.hrmtool.attendance;

import com.dayflow.hrmtool.attendance.dto.AttendanceDto;
import com.dayflow.hrmtool.attendance.dto.AttendanceRowDto;
import com.dayflow.hrmtool.attendance.dto.AttendanceSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.dayflow.hrmtool.auth.AppUser;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final com.dayflow.hrmtool.employee.EmployeeRepository employeeRepository;

    private Long getEmployeeId(AppUser currentUser) {
        if (currentUser.getEmployeeId() == null) {
            throw new com.dayflow.hrmtool.common.ResourceNotFoundException("Admin has no associated Employee profile");
        }
        return currentUser.getEmployeeId();
    }

    private Long resolveCompanyId(AppUser currentUser) {
        return employeeRepository.findById(getEmployeeId(currentUser))
                .orElseThrow(() -> new com.dayflow.hrmtool.common.ResourceNotFoundException("Employee profile not found"))
                .getCompanyId();
    }

    @PostMapping("/check-in")
    public ResponseEntity<AttendanceDto> checkIn(@AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.ok(attendanceService.checkIn(getEmployeeId(currentUser)));
    }
    
    @PostMapping("/check-out")
    public ResponseEntity<AttendanceDto> checkOut(@AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.ok(attendanceService.checkOut(getEmployeeId(currentUser)));
    }
    
    @GetMapping("/me")
    public ResponseEntity<List<AttendanceDto>> getMyAttendance(
            @AuthenticationPrincipal AppUser currentUser,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(attendanceService.getMyAttendance(getEmployeeId(currentUser), month, year));
    }
    
    @GetMapping("/me/summary")
    public ResponseEntity<AttendanceSummaryDto> getMySummary(
            @AuthenticationPrincipal AppUser currentUser,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(attendanceService.getMySummary(getEmployeeId(currentUser), month, year));
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AttendanceRowDto>> getForDate(
            @AuthenticationPrincipal AppUser currentUser,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ResponseEntity.ok(attendanceService.getForDate(resolveCompanyId(currentUser), date, search, pageable));
    }
}
