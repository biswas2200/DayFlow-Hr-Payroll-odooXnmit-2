package com.dayflow.hrmtool.attendance;

import com.dayflow.hrmtool.attendance.dto.AttendanceDto;
import com.dayflow.hrmtool.attendance.dto.AttendanceRowDto;
import com.dayflow.hrmtool.attendance.dto.AttendanceSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    
    @PostMapping("/check-in")
    public ResponseEntity<AttendanceDto> checkIn(@RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(attendanceService.checkIn(userId));
    }
    
    @PostMapping("/check-out")
    public ResponseEntity<AttendanceDto> checkOut(@RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(attendanceService.checkOut(userId));
    }
    
    @GetMapping("/me")
    public ResponseEntity<List<AttendanceDto>> getMyAttendance(
            @RequestAttribute("userId") Long userId,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(attendanceService.getMyAttendance(userId, month, year));
    }
    
    @GetMapping("/me/summary")
    public ResponseEntity<AttendanceSummaryDto> getMySummary(
            @RequestAttribute("userId") Long userId,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(attendanceService.getMySummary(userId, month, year));
    }
    
    @GetMapping("/")
    public ResponseEntity<List<AttendanceRowDto>> getForDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ResponseEntity.ok(attendanceService.getForDate(date, search, pageable));
    }
}
