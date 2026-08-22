package com.dayflow.hrmtool.auth;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginSerialCounterRepository extends JpaRepository<LoginSerialCounter, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<LoginSerialCounter> findByCompanyIdAndYear(Long companyId, int year);
}
