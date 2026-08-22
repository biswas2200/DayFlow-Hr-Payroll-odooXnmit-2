package com.dayflow.hrmtool.payroll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dayflow.hrmtool.payroll.dto.PayslipDto;
import com.dayflow.hrmtool.payroll.dto.SalaryStructureDto;
import com.dayflow.hrmtool.payroll.dto.SalaryStructureRequest;

import java.util.List;

@RestController
@RequestMapping("/api/payroll")
public class PayrollController {

    @Autowired
    private SalaryService salaryService;
    
    @Autowired
    private PayrollService payrollService;

    @GetMapping("/structure/{employeeId}")
    public SalaryStructureDto getStructure(@PathVariable Long employeeId) {
        return salaryService.getStructure(employeeId);
    }

    @PostMapping("/structure/{employeeId}")
    public SalaryStructureDto upsertStructure(
            @PathVariable Long employeeId,
            @RequestBody SalaryStructureRequest req,
            @RequestParam Long updatedBy) {
        return salaryService.upsertStructure(employeeId, req, updatedBy);
    }

    @PostMapping("/payslip/generate")
    public PayslipDto generatePayslip(
            @RequestParam Long employeeId,
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam Long generatedBy) {
        return payrollService.generatePayslip(employeeId, month, year, generatedBy);
    }

    @GetMapping("/payslip/my")
    public List<PayslipDto> listMyPayslips(@RequestParam Long employeeId) {
        return payrollService.listMyPayslips(employeeId);
    }
    
    @GetMapping("/payslip/employee/{employeeId}")
    public List<PayslipDto> listPayslipsByEmployee(@PathVariable Long employeeId) {
        return payrollService.listPayslipsByEmployee(employeeId);
    }
    
    @GetMapping("/payslip/{payslipId}/pdf")
    public ResponseEntity<byte[]> getPayslipPdf(
            @PathVariable Long payslipId,
            @RequestParam Long userId,
            @RequestParam Role role) {
        
        byte[] pdfBytes = payrollService.getPayslipPdf(payslipId, userId, role);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "payslip.pdf");
        
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
