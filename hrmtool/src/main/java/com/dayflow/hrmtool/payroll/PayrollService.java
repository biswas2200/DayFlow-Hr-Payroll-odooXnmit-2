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
        SalaryStructure structure = structureDto.getStructure();
        List<SalaryComponent> components = structureDto.getComponents();
        
        double payableDays = attendanceService.getPayableDays(employeeId, month, year);
        double totalWorkingDays = attendanceService.getTotalWorkingDays(month, year);
        
        double monthlyWage = structure.getMonthlyWage() != null ? structure.getMonthlyWage() : 0.0;
        double grossSalary = monthlyWage * (payableDays / totalWorkingDays);
        
        double basicAmount = 0.0;
        for (SalaryComponent c : components) {
            if (c.getType() == ComponentType.BASIC) {
                basicAmount = c.getComputedAmount() != null ? c.getComputedAmount() : 0.0;
                break;
            }
        }
        
        double pfPercent = structure.getPfEmployeePercent() != null ? structure.getPfEmployeePercent() : 0.0;
        double pfEmployee = basicAmount * (pfPercent / 100.0);
        double professionalTax = structure.getProfessionalTax() != null ? structure.getProfessionalTax() : 0.0;
        
        double deductions = pfEmployee + professionalTax;
        double netSalary = grossSalary - deductions;
        
        Payslip payslip = new Payslip();
        payslip.setEmployeeId(employeeId);
        payslip.setMonth(month);
        payslip.setYear(year);
        payslip.setGrossSalary(grossSalary);
        payslip.setPfEmployee(pfEmployee);
        payslip.setProfessionalTax(professionalTax);
        payslip.setNetSalary(netSalary);
        payslip.setGeneratedBy(generatedBy);
        payslip.setGeneratedAt(LocalDateTime.now());
        
        String employeeName = "Employee " + employeeId; // Mock name
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
    
    public byte[] getPayslipPdf(Long payslipId, Long userId, Role role) {
        Payslip payslip = payslipRepository.findById(payslipId)
                .orElseThrow(() -> new RuntimeException("Payslip not found"));
        
        if (role == Role.EMPLOYEE && !payslip.getEmployeeId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
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
        PayslipDto dto = new PayslipDto();
        dto.setId(p.getId());
        dto.setEmployeeId(p.getEmployeeId());
        dto.setMonth(p.getMonth());
        dto.setYear(p.getYear());
        dto.setGrossSalary(p.getGrossSalary());
        dto.setPfEmployee(p.getPfEmployee());
        dto.setProfessionalTax(p.getProfessionalTax());
        dto.setNetSalary(p.getNetSalary());
        dto.setPdfPath(p.getPdfPath());
        dto.setGeneratedAt(p.getGeneratedAt());
        return dto;
    }
}
