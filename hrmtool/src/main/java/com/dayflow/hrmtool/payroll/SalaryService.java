package com.dayflow.hrmtool.payroll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dayflow.hrmtool.payroll.dto.SalaryStructureDto;
import com.dayflow.hrmtool.payroll.dto.SalaryStructureRequest;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SalaryService {

    @Autowired
    private SalaryStructureRepository structureRepository;
    
    @Autowired
    private SalaryComponentRepository componentRepository;

    public SalaryStructureDto getStructure(Long employeeId) {
        SalaryStructure structure = structureRepository.findById(employeeId)
            .orElseThrow(() -> new com.dayflow.hrmtool.common.ResourceNotFoundException("Structure not found"));
        List<SalaryComponent> components = componentRepository.findBySalaryStructureId(employeeId);
        return toDto(structure, components);
    }

    @Transactional
    public SalaryStructureDto upsertStructure(Long employeeId, SalaryStructureRequest req, Long updatedBy) {
        SalaryStructure structure = structureRepository.findById(employeeId).orElse(new SalaryStructure());
        structure.setEmployeeId(employeeId);
        structure.setMonthlyWage(req.getMonthlyWage());
        structure.setYearlyWage(req.getMonthlyWage() != null ? req.getMonthlyWage() * 12 : null);
        structure.setWorkingDaysPerWeek(req.getWorkingDaysPerWeek());
        structure.setBreakHours(req.getBreakHours());
        structure.setPfEmployeePercent(req.getPfEmployeePercent());
        structure.setPfEmployerPercent(req.getPfEmployerPercent());
        structure.setProfessionalTax(req.getProfessionalTax());
        structure.setUpdatedAt(LocalDateTime.now());
        structure.setUpdatedBy(updatedBy);

        List<SalaryComponent> components = req.getComponents();
        recomputeComponents(structure, components);

        structure = structureRepository.save(structure);

        for (SalaryComponent c : components) {
            c.setSalaryStructureId(employeeId);
        }

        componentRepository.deleteBySalaryStructureId(employeeId);
        components = componentRepository.saveAll(components);

        return toDto(structure, components);
    }

    private SalaryStructureDto toDto(SalaryStructure structure, List<SalaryComponent> components) {
        return SalaryStructureDto.builder()
                .employeeId(structure.getEmployeeId())
                .monthlyWage(structure.getMonthlyWage())
                .yearlyWage(structure.getYearlyWage())
                .workingDaysPerWeek(structure.getWorkingDaysPerWeek())
                .breakHours(structure.getBreakHours())
                .components(components)
                .pfEmployeePercent(structure.getPfEmployeePercent())
                .pfEmployerPercent(structure.getPfEmployerPercent())
                .professionalTax(structure.getProfessionalTax())
                .build();
    }

    public void recomputeComponents(SalaryStructure structure, List<SalaryComponent> components) {
        double wage = structure.getMonthlyWage() != null ? structure.getMonthlyWage() : 0.0;
        
        double basicAmount = 0.0;
        SalaryComponent basicComponent = null;
        SalaryComponent fixedAllowanceComponent = null;
        
        for (SalaryComponent c : components) {
            if (c.getType() == ComponentType.BASIC) {
                basicComponent = c;
                if (c.getComputationType() == ComputationType.FIXED) {
                    basicAmount = c.getValue();
                } else {
                    basicAmount = wage * (c.getValue() / 100.0);
                }
                c.setComputedAmount(basicAmount);
            } else if (c.getType() == ComponentType.FIXED_ALLOWANCE) {
                fixedAllowanceComponent = c;
            }
        }
        
        double runningTotal = basicAmount;
        
        for (SalaryComponent c : components) {
            if (c.getType() != ComponentType.BASIC && c.getType() != ComponentType.FIXED_ALLOWANCE) {
                double amount = 0.0;
                if (c.getComputationType() == ComputationType.FIXED) {
                    amount = c.getValue();
                } else {
                    amount = basicAmount * (c.getValue() / 100.0);
                }
                c.setComputedAmount(amount);
                runningTotal += amount;
            }
        }
        
        double fixedAllowanceAmount = wage - runningTotal;
        if (fixedAllowanceAmount < 0) {
            throw new RuntimeException("Total components exceed monthly wage.");
        }

        if (fixedAllowanceComponent == null) {
            fixedAllowanceComponent = new SalaryComponent();
            fixedAllowanceComponent.setType(ComponentType.FIXED_ALLOWANCE);
            fixedAllowanceComponent.setComputationType(ComputationType.FIXED);
            components.add(fixedAllowanceComponent);
        }
        fixedAllowanceComponent.setValue(fixedAllowanceAmount);
        fixedAllowanceComponent.setComputedAmount(fixedAllowanceAmount);
    }
}
