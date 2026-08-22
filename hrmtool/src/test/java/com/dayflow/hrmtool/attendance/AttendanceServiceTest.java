package com.dayflow.hrmtool.attendance;

import com.dayflow.hrmtool.attendance.dto.AttendanceDto;
import com.dayflow.hrmtool.attendance.dto.AttendanceSummaryDto;
import com.dayflow.hrmtool.common.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AttendanceService — covers check-in/out, queries, and payable-days calculation.
 */
@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock private AttendanceRepository attendanceRepository;
    @InjectMocks private AttendanceService attendanceService;

    private static final Long EMPLOYEE_ID = 1L;
    private static final LocalDate TODAY = LocalDate.now();

    // =========== CHECK-IN TESTS ===========

    @Test
    void checkIn_noExistingRecord_createsAttendanceWithPresentStatus() {
        when(attendanceRepository.findByEmployeeIdAndDate(EMPLOYEE_ID, TODAY))
                .thenReturn(Optional.empty());
        Attendance saved = Attendance.builder()
                .employeeId(EMPLOYEE_ID)
                .date(TODAY)
                .checkInTime(LocalTime.of(9, 0))
                .status(AttendanceStatus.PRESENT)
                .build();
        when(attendanceRepository.save(any())).thenReturn(saved);

        AttendanceDto result = attendanceService.checkIn(EMPLOYEE_ID);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(AttendanceStatus.PRESENT);
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    void checkIn_alreadyCheckedInToday_throwsException() {
        Attendance existing = Attendance.builder()
                .employeeId(EMPLOYEE_ID)
                .date(TODAY)
                .checkInTime(LocalTime.of(9, 0))
                .status(AttendanceStatus.PRESENT)
                .build();
        when(attendanceRepository.findByEmployeeIdAndDate(EMPLOYEE_ID, TODAY))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> attendanceService.checkIn(EMPLOYEE_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Already checked in");
    }

    // =========== CHECK-OUT TESTS ===========

    @Test
    void checkOut_afterCheckIn_calculatesWorkHours() {
        Attendance checkedIn = Attendance.builder()
                .employeeId(EMPLOYEE_ID)
                .date(TODAY)
                .checkInTime(LocalTime.of(9, 0))
                .status(AttendanceStatus.PRESENT)
                .build();
        when(attendanceRepository.findByEmployeeIdAndDate(EMPLOYEE_ID, TODAY))
                .thenReturn(Optional.of(checkedIn));
        when(attendanceRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AttendanceDto result = attendanceService.checkOut(EMPLOYEE_ID);

        assertThat(result.getCheckOutTime()).isNotNull();
        assertThat(result.getWorkHours()).isNotNull();
        verify(attendanceRepository).save(checkedIn);
    }

    @Test
    void checkOut_notCheckedIn_throwsException() {
        when(attendanceRepository.findByEmployeeIdAndDate(EMPLOYEE_ID, TODAY))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.checkOut(EMPLOYEE_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Not checked in");
    }

    @Test
    void checkOut_alreadyCheckedOut_throwsException() {
        Attendance alreadyOut = Attendance.builder()
                .employeeId(EMPLOYEE_ID)
                .date(TODAY)
                .checkInTime(LocalTime.of(9, 0))
                .checkOutTime(LocalTime.of(18, 0))
                .status(AttendanceStatus.PRESENT)
                .build();
        when(attendanceRepository.findByEmployeeIdAndDate(EMPLOYEE_ID, TODAY))
                .thenReturn(Optional.of(alreadyOut));

        assertThatThrownBy(() -> attendanceService.checkOut(EMPLOYEE_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Already checked out");
    }

    // =========== MY ATTENDANCE TESTS ===========

    @Test
    void getMyAttendance_returnsListForMonth() {
        LocalDate start = LocalDate.of(2025, 8, 1);
        LocalDate end = LocalDate.of(2025, 8, 31);
        List<Attendance> records = List.of(
                Attendance.builder().employeeId(EMPLOYEE_ID).date(LocalDate.of(2025, 8, 1))
                        .checkInTime(LocalTime.of(9, 0)).status(AttendanceStatus.PRESENT).build(),
                Attendance.builder().employeeId(EMPLOYEE_ID).date(LocalDate.of(2025, 8, 2))
                        .checkInTime(LocalTime.of(9, 30)).status(AttendanceStatus.PRESENT).build()
        );
        when(attendanceRepository.findByEmployeeIdAndDateBetween(EMPLOYEE_ID, start, end))
                .thenReturn(records);

        List<AttendanceDto> result = attendanceService.getMyAttendance(EMPLOYEE_ID, 8, 2025);

        assertThat(result).hasSize(2);
    }

    @Test
    void getMyAttendance_noRecordsInMonth_returnsEmptyList() {
        when(attendanceRepository.findByEmployeeIdAndDateBetween(anyLong(), any(), any()))
                .thenReturn(List.of());

        List<AttendanceDto> result = attendanceService.getMyAttendance(EMPLOYEE_ID, 1, 2025);
        assertThat(result).isEmpty();
    }

    // =========== SUMMARY TESTS ===========

    @Test
    void getMySummary_countsPresentAndLeaves() {
        when(attendanceRepository.countByEmployeeIdAndStatusInAndDateBetween(
                eq(EMPLOYEE_ID), eq(List.of(AttendanceStatus.PRESENT)), any(), any()))
                .thenReturn(15L);
        when(attendanceRepository.countByEmployeeIdAndStatusInAndDateBetween(
                eq(EMPLOYEE_ID), eq(List.of(AttendanceStatus.HALF_DAY)), any(), any()))
                .thenReturn(2L);
        when(attendanceRepository.countByEmployeeIdAndStatusInAndDateBetween(
                eq(EMPLOYEE_ID), eq(List.of(AttendanceStatus.ON_LEAVE)), any(), any()))
                .thenReturn(3L);

        AttendanceSummaryDto summary = attendanceService.getMySummary(EMPLOYEE_ID, 8, 2025);

        assertThat(summary.getDaysPresent()).isEqualTo(16.0); // 15 + (2 * 0.5)
        assertThat(summary.getLeavesCount()).isEqualTo(3);
        assertThat(summary.getMonth()).isEqualTo(8);
        assertThat(summary.getYear()).isEqualTo(2025);
    }

    @Test
    void getMySummary_allAbsent_zeroPresent() {
        when(attendanceRepository.countByEmployeeIdAndStatusInAndDateBetween(
                eq(EMPLOYEE_ID), eq(List.of(AttendanceStatus.PRESENT)), any(), any()))
                .thenReturn(0L);
        when(attendanceRepository.countByEmployeeIdAndStatusInAndDateBetween(
                eq(EMPLOYEE_ID), eq(List.of(AttendanceStatus.HALF_DAY)), any(), any()))
                .thenReturn(0L);
        when(attendanceRepository.countByEmployeeIdAndStatusInAndDateBetween(
                eq(EMPLOYEE_ID), eq(List.of(AttendanceStatus.ON_LEAVE)), any(), any()))
                .thenReturn(0L);

        AttendanceSummaryDto summary = attendanceService.getMySummary(EMPLOYEE_ID, 1, 2025);
        assertThat(summary.getDaysPresent()).isZero();
        assertThat(summary.getLeavesCount()).isZero();
    }

    // =========== PAYABLE DAYS TESTS ===========

    @Test
    void getPayableDays_basedOnPresentCount() {
        when(attendanceRepository.countByEmployeeIdAndStatusInAndDateBetween(
                eq(EMPLOYEE_ID), eq(List.of(AttendanceStatus.PRESENT)), any(), any()))
                .thenReturn(20L);
        when(attendanceRepository.countByEmployeeIdAndStatusInAndDateBetween(
                eq(EMPLOYEE_ID), eq(List.of(AttendanceStatus.HALF_DAY)), any(), any()))
                .thenReturn(0L);
        when(attendanceRepository.countByEmployeeIdAndStatusInAndDateBetween(
                eq(EMPLOYEE_ID), eq(List.of(AttendanceStatus.ON_LEAVE)), any(), any()))
                .thenReturn(2L);

        int payable = attendanceService.getPayableDays(EMPLOYEE_ID, 8, 2025);
        assertThat(payable).isGreaterThan(0);
    }
}
