package com.dayflow.hrmtool.leave;

import com.dayflow.hrmtool.auth.AppUser;
import com.dayflow.hrmtool.common.ResourceNotFoundException;
import com.dayflow.hrmtool.employee.Employee;
import com.dayflow.hrmtool.employee.EmployeeRepository;
import com.dayflow.hrmtool.leave.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Leave module REST controller — aligned with the frontend API contract in
 * documentation/design/09-api-documentation.md.
 *
 * URL base: /api/v1  (matching frontend LeaveService which calls /api/v1/leave-types etc.)
 * Identity is always resolved from the JWT principal — no employeeId/companyId query params
 * needed on the authenticated employee-facing endpoints.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;
    private final EmployeeRepository employeeRepository;

    // ─── Helper ──────────────────────────────────────────────────────────────

    private Employee resolveEmployee(AppUser currentUser) {
        Long empId = currentUser.getEmployeeId();
        if (empId == null) {
            throw new ResourceNotFoundException("Admin has no associated Employee profile");
        }
        return employeeRepository.findById(empId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found"));
    }

    // ─── Leave Types ─────────────────────────────────────────────────────────

    /** GET /api/v1/leave-types — SRS §3.5.1 */
    @GetMapping("/leave-types")
    public ResponseEntity<List<LeaveTypeDto>> listTypes(@AuthenticationPrincipal AppUser currentUser) {
        Employee employee = resolveEmployee(currentUser);
        return ResponseEntity.ok(leaveService.listTypes(employee.getCompanyId()));
    }

    // ─── Leave Balances ───────────────────────────────────────────────────────

    /** GET /api/v1/leave-balances/me — SRS §3.5.1 */
    @GetMapping("/leave-balances/me")
    public ResponseEntity<List<LeaveBalanceDto>> getMyBalances(
            @AuthenticationPrincipal AppUser currentUser,
            @RequestParam(defaultValue = "0") int year) {
        Employee employee = resolveEmployee(currentUser);
        int resolvedYear = year == 0 ? LocalDate.now().getYear() : year;
        return ResponseEntity.ok(leaveService.getMyBalances(employee.getId(), employee.getCompanyId(), resolvedYear));
    }

    // ─── Leave Requests ───────────────────────────────────────────────────────

    /** POST /api/v1/leave-requests — apply for leave, multipart — SRS §3.5.2 */
    @PostMapping(value = "/leave-requests", consumes = {"multipart/form-data", "application/json"})
    public ResponseEntity<LeaveRequestDto> applyLeave(
            @AuthenticationPrincipal AppUser currentUser,
            @ModelAttribute ApplyLeaveRequest dto) {
        Employee employee = resolveEmployee(currentUser);
        return ResponseEntity.status(201).body(leaveService.apply(employee.getId(), employee.getCompanyId(), dto));
    }

    /** GET /api/v1/leave-requests/me — own calendar feed — SRS §3.5.2 */
    @GetMapping("/leave-requests/me")
    public ResponseEntity<LeaveCalendarDto> getMyCalendar(
            @AuthenticationPrincipal AppUser currentUser,
            @RequestParam(defaultValue = "0") int year) {
        Employee employee = resolveEmployee(currentUser);
        int resolvedYear = year == 0 ? LocalDate.now().getYear() : year;
        return ResponseEntity.ok(leaveService.getMyCalendar(employee.getId(), employee.getCompanyId(), resolvedYear));
    }

    /** GET /api/v1/leave-requests — admin approval queue — SRS §3.5.3 */
    @GetMapping("/leave-requests")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LeaveRequestDto>> getAllRequests(
            @RequestParam(required = false) LeaveStatus status,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(
                leaveService.listAllForApproval(status, page).getContent()
        );
    }

    /** PATCH /api/v1/leave-requests/{id}/approve — SRS §3.5.3 */
    @PatchMapping("/leave-requests/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> approve(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUser currentUser,
            @RequestBody(required = false) ApproveRejectRequest body) {
        String comment = body != null ? body.getComment() : null;
        leaveService.approve(id, currentUser.getId(), comment);
        return ResponseEntity.noContent().build();
    }

    /** PATCH /api/v1/leave-requests/{id}/reject — SRS §3.5.3 */
    @PatchMapping("/leave-requests/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reject(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUser currentUser,
            @RequestBody(required = false) ApproveRejectRequest body) {
        String comment = body != null ? body.getComment() : null;
        leaveService.reject(id, currentUser.getId(), comment);
        return ResponseEntity.noContent().build();
    }

    // ─── Leave Allocations ────────────────────────────────────────────────────

    /** GET /api/v1/leave-allocations/{employeeId} — SRS §3.5.4 */
    @GetMapping("/leave-allocations/{employeeId}")
    public ResponseEntity<List<LeaveAllocationDto>> getAllocations(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "0") int year) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        int resolvedYear = year == 0 ? LocalDate.now().getYear() : year;
        return ResponseEntity.ok(leaveService.getAllocation(employeeId, employee.getCompanyId(), resolvedYear));
    }

    /** PUT /api/v1/leave-allocations/{employeeId} — Admin only — SRS §3.5.4 */
    @PutMapping("/leave-allocations/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LeaveAllocationDto>> updateAllocations(
            @PathVariable Long employeeId,
            @RequestBody List<LeaveAllocationDto> dtos) {
        return ResponseEntity.ok(leaveService.setAllocation(employeeId, dtos));
    }

    // ─── Public Holidays ──────────────────────────────────────────────────────

    /** GET /api/v1/public-holidays — SRS §3.5.5 */
    @GetMapping("/public-holidays")
    public ResponseEntity<List<PublicHolidayDto>> getPublicHolidays(
            @AuthenticationPrincipal AppUser currentUser,
            @RequestParam(defaultValue = "0") int year) {
        Employee employee = resolveEmployee(currentUser);
        int resolvedYear = year == 0 ? LocalDate.now().getYear() : year;
        return ResponseEntity.ok(leaveService.getPublicHolidays(employee.getCompanyId(), resolvedYear));
    }
}
