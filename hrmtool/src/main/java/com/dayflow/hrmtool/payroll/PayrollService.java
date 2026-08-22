package com.dayflow.hrmtool.payroll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dayflow.hrmtool.payroll.dto.PayslipDto;
import com.dayflow.hrmtool.payroll.dto.SalaryStructureDto;
import com.dayflow.hrmtool.attendance.AttendanceService;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PayrollService {

    @Autowired
    private SalaryService salaryService;
    
    @Autowired
    private AttendanceService attendanceService;
    
    @Autowired
    private PayslipRepository payslipRepository;
    
    @Autowired
    private PayslipPdfService pdfService;

    public PayslipDto generatePayslip(Long employeeId, int month, int year, Long generatedBy) {
        SalaryStructureDto structureDto = salaryService.getStructure(employeeId);
        List<SalaryComponent> components = structureDto.getComponents();

        double payableDays = attendanceService.getPayableDays(employeeId, month, year);
        double totalWorkingDays = attendanceService.getTotalWorkingDays(month, year);

        double monthlyWage = structureDto.getMonthlyWage() != null ? structureDto.getMonthlyWage() : 0.0;
        double grossSalary = monthlyWage * (payableDays / totalWorkingDays);

        double basicAmount = 0.0;
        for (SalaryComponent c : components) {
            if (c.getType() == ComponentType.BASIC) {
                basicAmount = c.getComputedAmount() != null ? c.getComputedAmount() : 0.0;
                break;
            }
        }

        double pfPercent = structureDto.getPfEmployeePercent() != null ? structureDto.getPfEmployeePercent() : 0.0;
        double pfEmployee = basicAmount * (pfPercent / 100.0);
        double professionalTax = structureDto.getProfessionalTax() != null ? structureDto.getProfessionalTax() : 0.0;

        double deductions = pfEmployee + professionalTax;
        double netSalary = grossSalary - deductions;

        Payslip payslip = new Payslip();
        payslip.setEmployeeId(employeeId);
        payslip.setMonth(month);
        payslip.setYear(year);
        payslip.setPayableDays(payableDays);
        payslip.setGrossSalary(grossSalary);
        payslip.setPfEmployee(pfEmployee);
        payslip.setProfessionalTax(professionalTax);
        payslip.setNetSalary(netSalary);
        payslip.setGeneratedBy(generatedBy);
        payslip.setGeneratedAt(LocalDateTime.now());

        String employeeName = "Employee " + employeeId; // Mock name
        SalaryStructure structure = new SalaryStructure();
        structure.setMonthlyWage(structureDto.getMonthlyWage());
        structure.setPfEmployeePercent(structureDto.getPfEmployeePercent());
        structure.setProfessionalTax(structureDto.getProfessionalTax());
        String pdfPath = pdfService.generatePdfAndSave(payslip, structure, components, employeeName);
        payslip.setPdfPath(pdfPath);

        payslip = payslipRepository.save(payslip);
        return mapToDto(payslip);
    }

    public List<PayslipDto> listMyPayslips(Long employeeId) {
        return payslipRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<PayslipDto> listPayslipsByEmployee(Long employeeId) {
        return listMyPayslips(employeeId);
    }
    
    public byte[] getPayslipPdf(Long payslipId, Long userId, com.dayflow.hrmtool.auth.Role role) {
        Payslip payslip = payslipRepository.findById(payslipId)
                .orElseThrow(() -> new com.dayflow.hrmtool.common.ResourceNotFoundException("Payslip not found"));
        
        if (role == com.dayflow.hrmtool.auth.Role.EMPLOYEE && !payslip.getEmployeeId().equals(userId)) {
            throw new com.dayflow.hrmtool.common.AuthenticationException("Unauthorized");
        }
        
        try {
            File pdfFile = new File("uploads/" + payslip.getPdfPath());
            if (pdfFile.exists()) {
                return Files.readAllBytes(pdfFile.toPath());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading PDF", e);
        }
        return new byte[0];
    }
    
    private PayslipDto mapToDto(Payslip p) {
        double pfEmployee = p.getPfEmployee() != null ? p.getPfEmployee() : 0.0;
        double professionalTax = p.getProfessionalTax() != null ? p.getProfessionalTax() : 0.0;

        PayslipDto dto = new PayslipDto();
        dto.setId(p.getId());
        dto.setEmployeeId(p.getEmployeeId());
        dto.setMonth(p.getMonth());
        dto.setYear(p.getYear());
        dto.setPayableDays(p.getPayableDays());
        dto.setGrossSalary(p.getGrossSalary());
        dto.setTotalDeductions(pfEmployee + professionalTax);
        dto.setNetSalary(p.getNetSalary());
        dto.setPdfUrl(p.getPdfPath());
        dto.setGeneratedAt(p.getGeneratedAt());
        return dto;
    }
}
