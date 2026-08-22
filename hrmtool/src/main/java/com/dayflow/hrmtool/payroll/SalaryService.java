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
            .orElseThrow(() -> new RuntimeException("Structure not found"));
        List<SalaryComponent> components = componentRepository.findBySalaryStructureId(employeeId);
        return new SalaryStructureDto(structure, components);
    }

    @Transactional
    public SalaryStructureDto upsertStructure(Long employeeId, SalaryStructureRequest req, Long updatedBy) {
        SalaryStructure structure = req.getStructure();
        structure.setEmployeeId(employeeId);
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
        
        return new SalaryStructureDto(structure, components);
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
        
        if (fixedAllowanceComponent != null) {
            double fixedAllowanceAmount = wage - runningTotal;
            if (fixedAllowanceAmount < 0) {
                throw new RuntimeException("Fixed Allowance cannot be negative. Adjust percentages or wage.");
            }
            fixedAllowanceComponent.setComputedAmount(fixedAllowanceAmount);
        } else {
            if (wage < runningTotal) {
                throw new RuntimeException("Total components exceed monthly wage.");
            }
        }
    }
}
