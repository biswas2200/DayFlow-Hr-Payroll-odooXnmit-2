package com.dayflow.hrmtool.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByLoginId(String loginId);
    Optional<AppUser> findByEmail(String email);
    Optional<AppUser> findByLoginIdOrEmail(String loginId, String email);
    Optional<AppUser> findByEmployeeId(Long employeeId);
    List<AppUser> findByRole(Role role);
}
