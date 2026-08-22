package com.dayflow.hrmtool.payroll;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalaryComponentRepository extends JpaRepository<SalaryComponent, Long> {
    List<SalaryComponent> findBySalaryStructureId(Long salaryStructureId);
    void deleteBySalaryStructureId(Long salaryStructureId);
}
