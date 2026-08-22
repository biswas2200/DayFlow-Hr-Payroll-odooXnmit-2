package com.dayflow.hrmtool.attendance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByEmployeeIdAndDate(Long employeeId, LocalDate date);
    List<Attendance> findByEmployeeIdAndDateBetween(Long employeeId, LocalDate start, LocalDate end);
    List<Attendance> findByDateAndEmployeeIdIn(LocalDate date, List<Long> employeeIds);
    long countByEmployeeIdAndStatusInAndDateBetween(Long employeeId, List<AttendanceStatus> statuses, LocalDate start, LocalDate end);
    List<Attendance> findByDateAndCheckInTimeIsNotNullAndCheckOutTimeIsNull(LocalDate date);
}
