package com.dayflow.hrmtool.leave;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {
    List<LeaveType> findByCompanyId(Long companyId);
}
