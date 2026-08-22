package com.dayflow.hrmtool.attendance;

import com.dayflow.hrmtool.attendance.dto.AttendanceDto;
import com.dayflow.hrmtool.attendance.dto.AttendanceRowDto;
import com.dayflow.hrmtool.attendance.dto.AttendanceSummaryDto;
import com.dayflow.hrmtool.employee.Employee;
import com.dayflow.hrmtool.employee.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    
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
    
    public List<AttendanceRowDto> getForDate(Long companyId, LocalDate date, String search, Pageable pageable) {
        List<Employee> employees = (search != null && !search.trim().isEmpty())
                ? employeeRepository.searchDirectory(companyId, search, Pageable.unpaged()).getContent()
                : employeeRepository.findByCompanyId(companyId);

        List<Long> employeeIds = employees.stream().map(Employee::getId).collect(Collectors.toList());
        Map<Long, Attendance> attendanceByEmployee = attendanceRepository.findByDateAndEmployeeIdIn(date, employeeIds).stream()
                .collect(Collectors.toMap(Attendance::getEmployeeId, a -> a));

        List<AttendanceRowDto> rows = employees.stream().map(e -> {
            Attendance a = attendanceByEmployee.get(e.getId());
            return AttendanceRowDto.builder()
                    .id(a != null ? a.getId() : null)
                    .date(date)
                    .checkInTime(a != null ? a.getCheckInTime() : null)
                    .checkOutTime(a != null ? a.getCheckOutTime() : null)
                    .workHours(a != null ? formatMinutes(a.getWorkHours()) : "00:00")
                    .extraHours(a != null ? formatMinutes(a.getExtraHours()) : "00:00")
                    .status(a != null ? a.getStatus() : AttendanceStatus.ABSENT)
                    .employeeId(e.getId())
                    .employeeName(e.getFirstName() + " " + e.getLastName())
                    .build();
        }).collect(Collectors.toList());

        int start = Math.min((int) pageable.getOffset(), rows.size());
        int end = Math.min(start + (pageable.isPaged() ? pageable.getPageSize() : rows.size()), rows.size());
        return rows.subList(start, end);
    }
    
    public int getPayableDays(Long employeeId, int month, int year) {
        AttendanceSummaryDto summary = getMySummary(employeeId, month, year);
        return (int) summary.getDaysPresent(); // simplified logic for payable days
    }
    
    public int getTotalWorkingDays(int month, int year) {
        return 22; // simplified logic for now
    }
    
    private AttendanceDto mapToDto(Attendance attendance) {
        return AttendanceDto.builder()
                .id(attendance.getId())
                .date(attendance.getDate())
                .checkInTime(attendance.getCheckInTime())
                .checkOutTime(attendance.getCheckOutTime())
                .workHours(formatMinutes(attendance.getWorkHours()))
                .extraHours(formatMinutes(attendance.getExtraHours()))
                .status(attendance.getStatus())
                .build();
    }

    private String formatMinutes(Long minutes) {
        if (minutes == null) return "00:00";
        long hours = minutes / 60;
        long mins = minutes % 60;
        return String.format("%02d:%02d", hours, mins);
    }
}
