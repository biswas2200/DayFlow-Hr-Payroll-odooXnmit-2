package com.dayflow.hrmtool.leave;

import com.dayflow.hrmtool.leave.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @GetMapping("/types")
    public ResponseEntity<List<LeaveTypeDto>> listTypes(@RequestParam Long companyId) {
        return ResponseEntity.ok(leaveService.listTypes(companyId));
    }

    @GetMapping("/balances")
    public ResponseEntity<List<LeaveBalanceDto>> getMyBalances(@RequestParam Long employeeId, @RequestParam Long companyId, @RequestParam int year) {
        return ResponseEntity.ok(leaveService.getMyBalances(employeeId, companyId, year));
    }

    @PostMapping("/apply")
    public ResponseEntity<LeaveRequestDto> applyLeave(@RequestParam Long employeeId, @RequestParam Long companyId, @RequestBody ApplyLeaveRequest dto) {
        return ResponseEntity.ok(leaveService.apply(employeeId, companyId, dto));
    }

    @GetMapping("/calendar")
    public ResponseEntity<LeaveCalendarDto> getMyCalendar(@RequestParam Long employeeId, @RequestParam Long companyId, @RequestParam int year) {
        return ResponseEntity.ok(leaveService.getMyCalendar(employeeId, companyId, year));
    }

    @GetMapping("/approvals")
    public ResponseEntity<Page<LeaveRequestDto>> listAllForApproval(@RequestParam LeaveStatus status, Pageable pageable) {
        return ResponseEntity.ok(leaveService.listAllForApproval(status, pageable));
    }

    @PostMapping("/{requestId}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long requestId, @RequestParam Long approverId, @RequestBody String comment) {
        leaveService.approve(requestId, approverId, comment);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{requestId}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long requestId, @RequestParam Long approverId, @RequestBody String comment) {
        leaveService.reject(requestId, approverId, comment);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/allocations")
    public ResponseEntity<List<LeaveAllocationDto>> getAllocation(@RequestParam Long employeeId, @RequestParam Long companyId, @RequestParam int year) {
        return ResponseEntity.ok(leaveService.getAllocation(employeeId, companyId, year));
    }

    @PostMapping("/allocations")
    public ResponseEntity<List<LeaveAllocationDto>> setAllocation(@RequestParam Long employeeId, @RequestBody List<LeaveAllocationDto> dtos) {
        return ResponseEntity.ok(leaveService.setAllocation(employeeId, dtos));
    }

    @GetMapping("/holidays")
    public ResponseEntity<List<PublicHolidayDto>> getPublicHolidays(@RequestParam Long companyId, @RequestParam int year) {
        return ResponseEntity.ok(leaveService.getPublicHolidays(companyId, year));
    }
}
