package com.dayflow.hrmtool.payroll;

import com.dayflow.hrmtool.auth.AppUser;
import com.dayflow.hrmtool.common.ResourceNotFoundException;
import com.dayflow.hrmtool.employee.EmployeeRepository;
import com.dayflow.hrmtool.payroll.dto.GeneratePayslipRequest;
import com.dayflow.hrmtool.payroll.dto.PayslipDto;
import com.dayflow.hrmtool.payroll.dto.SalaryStructureDto;
import com.dayflow.hrmtool.payroll.dto.SalaryStructureRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Payroll REST controller aligned with the frontend API contract in
 * documentation/design/09-api-documentation.md.
 *
 * Endpoints:
 *   GET  /api/v1/employees/{id}/salary      — salary structure (owner read-only or admin full)
 *   PUT  /api/v1/employees/{id}/salary      — update salary structure (admin only)
 *   POST /api/v1/payslips/generate          — generate payslip (admin only, JSON body)
 *   GET  /api/v1/payslips/me               — own payslip history
 *   GET  /api/v1/payslips/{id}/download    — download payslip PDF
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PayrollController {

    private final SalaryService salaryService;
    private final PayrollService payrollService;
    private final EmployeeRepository employeeRepository;

    // ─── Salary Structure (under /employees/{id}/salary) ────────────────────

    /**
     * GET /api/v1/employees/{id}/salary
     * Owner gets read-only view; Admin gets full detail.
     */
    @GetMapping("/employees/{id}/salary")
    public ResponseEntity<SalaryStructureDto> getSalaryStructure(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.ok(salaryService.getStructure(id));
    }

    /**
     * PUT /api/v1/employees/{id}/salary — Admin only.
     * Configure wage & salary components.
     */
    @PutMapping("/employees/{id}/salary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SalaryStructureDto> updateSalaryStructure(
            @PathVariable Long id,
            @RequestBody SalaryStructureRequest request,
            @AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.ok(salaryService.upsertStructure(id, request, currentUser.getId()));
    }

    // ─── Payslips ────────────────────────────────────────────────────────────

    /**
     * POST /api/v1/payslips/generate — Admin only.
     * Body: { employeeId, month, year }
     */
    @PostMapping("/payslips/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PayslipDto> generatePayslip(
            @RequestBody GeneratePayslipRequest request,
            @AuthenticationPrincipal AppUser currentUser) {
        PayslipDto payslip = payrollService.generatePayslip(
                request.getEmployeeId(),
                request.getMonth(),
                request.getYear(),
                currentUser.getId());
        return ResponseEntity.status(201).body(payslip);
    }

    /**
     * GET /api/v1/payslips/me — Own payslip history.
     */
    @GetMapping("/payslips/me")
    public ResponseEntity<List<PayslipDto>> getMyPayslips(@AuthenticationPrincipal AppUser currentUser) {
        Long empId = currentUser.getEmployeeId();
        if (empId == null) {
            throw new ResourceNotFoundException("Admin has no associated Employee profile");
        }
        return ResponseEntity.ok(payrollService.listMyPayslips(empId));
    }

    /**
     * GET /api/v1/payslips/{employeeId} — Admin: any employee's payslip history.
     */
    @GetMapping("/payslips/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PayslipDto>> getPayslipsByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(payrollService.listPayslipsByEmployee(employeeId));
    }

    /**
     * GET /api/v1/payslips/{id}/download — PDF download (owner or Admin).
     */
    @GetMapping("/payslips/{id}/download")
    public ResponseEntity<byte[]> downloadPayslip(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUser currentUser) {

        com.dayflow.hrmtool.auth.Role role = currentUser.getRole();
        Long userId = currentUser.getEmployeeId() != null ? currentUser.getEmployeeId() : currentUser.getId();
        byte[] pdfBytes = payrollService.getPayslipPdf(id, userId, role);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "payslip.pdf");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}
