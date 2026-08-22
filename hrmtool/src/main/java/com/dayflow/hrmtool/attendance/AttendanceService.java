package com.dayflow.hrmtool.attendance;

import com.dayflow.hrmtool.attendance.dto.AttendanceDto;
import com.dayflow.hrmtool.attendance.dto.AttendanceRowDto;
import com.dayflow.hrmtool.attendance.dto.AttendanceSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    
    public AttendanceDto checkIn(Long employeeId) {
        LocalDate today = LocalDate.now();
        if (attendanceRepository.findByEmployeeIdAndDate(employeeId, today).isPresent()) {
            throw new RuntimeException("Already checked in today");
        }
        
        Attendance attendance = Attendance.builder()
                .employeeId(employeeId)
                .date(today)
                .checkInTime(LocalTime.now())
                .status(AttendanceStatus.PRESENT)
                .build();
                
        attendance = attendanceRepository.save(attendance);
        return mapToDto(attendance);
    }
    
    public AttendanceDto checkOut(Long employeeId) {
        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByEmployeeIdAndDate(employeeId, today)
                .orElseThrow(() -> new RuntimeException("Not checked in today"));
                
        if (attendance.getCheckOutTime() != null) {
            throw new RuntimeException("Already checked out today");
        }
        
        attendance.setCheckOutTime(LocalTime.now());
        long workMinutes = ChronoUnit.MINUTES.between(attendance.getCheckInTime(), attendance.getCheckOutTime());
        attendance.setWorkHours(workMinutes);
        
        long standardMinutes = 8 * 60;
        attendance.setExtraHours(Math.max(0L, workMinutes - standardMinutes));
        
        attendance = attendanceRepository.save(attendance);
        return mapToDto(attendance);
    }
    
    public List<AttendanceDto> getMyAttendance(Long employeeId, int month, int year) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        
        return attendanceRepository.findByEmployeeIdAndDateBetween(employeeId, start, end).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    public AttendanceSummaryDto getMySummary(Long employeeId, int month, int year) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        
        long presentCount = attendanceRepository.countByEmployeeIdAndStatusInAndDateBetween(
                employeeId, List.of(AttendanceStatus.PRESENT), start, end);
        long halfDayCount = attendanceRepository.countByEmployeeIdAndStatusInAndDateBetween(
                employeeId, List.of(AttendanceStatus.HALF_DAY), start, end);
                
        double daysPresent = presentCount + (halfDayCount * 0.5);
        
        long leavesCount = attendanceRepository.countByEmployeeIdAndStatusInAndDateBetween(
                employeeId, List.of(AttendanceStatus.ON_LEAVE), start, end);
                
        int totalWorkingDays = 22; // Fetch from company settings ideally
        
        return AttendanceSummaryDto.builder()
                .daysPresent(daysPresent)
                .leavesCount((int) leavesCount)
                .totalWorkingDays(totalWorkingDays)
                .month(month)
                .year(year)
                .build();
    }
    
    public List<AttendanceRowDto> getForDate(LocalDate date, String search, Pageable pageable) {
        // Implement complex search and mapping logic here
        return List.of(); 
    }
    
    public int getPayableDays(Long employeeId, int month, int year) {
        AttendanceSummaryDto summary = getMySummary(employeeId, month, year);
        return (int) summary.getDaysPresent(); // simplified logic for payable days
    }
    
    public int getTotalWorkingDays(int month, int year) {
        return 22; // simplified logic for now
    }
    
    private AttendanceDto mapToDto(Attendance attendance) {
        String workHoursStr = "00:00";
        if (attendance.getWorkHours() != null) {
            long hours = attendance.getWorkHours() / 60;
            long mins = attendance.getWorkHours() % 60;
            workHoursStr = String.format("%02d:%02d", hours, mins);
        }
        
        return AttendanceDto.builder()
                .id(attendance.getId())
                .date(attendance.getDate())
                .checkInTime(attendance.getCheckInTime())
                .checkOutTime(attendance.getCheckOutTime())
                .workHours(workHoursStr)
                .extraHours(attendance.getExtraHours())
                .status(attendance.getStatus())
                .build();
    }
}
