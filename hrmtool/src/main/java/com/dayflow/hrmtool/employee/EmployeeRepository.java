package com.dayflow.hrmtool.employee;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    @Query("SELECT e FROM Employee e WHERE e.companyId = :companyId AND (LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Employee> searchDirectory(@Param("companyId") Long companyId, @Param("search") String search, Pageable pageable);
    
    Page<Employee> findByCompanyId(Long companyId, Pageable pageable);
    Optional<Employee> findByWorkEmail(String email);
    int countByCompanyIdAndYearOfJoining(Long companyId, int year);
}
