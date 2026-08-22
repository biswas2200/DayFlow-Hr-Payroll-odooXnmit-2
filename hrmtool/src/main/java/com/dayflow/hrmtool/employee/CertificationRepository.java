package com.dayflow.hrmtool.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    void deleteByEmployeeId(Long employeeId);
    List<Certification> findByEmployeeId(Long employeeId);
}
