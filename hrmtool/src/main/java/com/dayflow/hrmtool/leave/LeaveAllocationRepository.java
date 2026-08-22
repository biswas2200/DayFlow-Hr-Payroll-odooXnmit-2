package com.dayflow.hrmtool.leave;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LeaveAllocationRepository extends JpaRepository<LeaveAllocation, Long> {
    List<LeaveAllocation> findByEmployeeIdAndYear(Long employeeId, int year);
    Optional<LeaveAllocation> findByEmployeeIdAndLeaveTypeIdAndYear(Long employeeId, Long leaveTypeId, int year);
}
