package com.dayflow.hrmtool.config;

import com.dayflow.hrmtool.auth.AppUser;
import com.dayflow.hrmtool.auth.AppUserRepository;
import com.dayflow.hrmtool.auth.Role;
import com.dayflow.hrmtool.company.Company;
import com.dayflow.hrmtool.company.CompanyRepository;
import com.dayflow.hrmtool.employee.BankDetail;
import com.dayflow.hrmtool.employee.BankDetailRepository;
import com.dayflow.hrmtool.employee.Employee;
import com.dayflow.hrmtool.employee.EmployeeRepository;
import com.dayflow.hrmtool.employee.EmployeeStatus;
import com.dayflow.hrmtool.employee.Resume;
import com.dayflow.hrmtool.employee.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final ResumeRepository resumeRepository;
    private final BankDetailRepository bankDetailRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Company company;
        if (companyRepository.count() == 0) {
            log.info("Seeding default company...");
            company = new Company();
            company.setName("Default Company");
            company.setInitials("DC");
            company.setWorkingDaysPerWeek(5);
            company.setBreakHours(BigDecimal.ONE);
            company = companyRepository.save(company);
        } else {
            company = companyRepository.findAll().get(0);
        }

        if (appUserRepository.findByLoginId("admin").isEmpty()) {
            log.info("Seeding default admin user with linked Employee profile...");

            Employee employee = Employee.builder()
                    .companyId(company.getId())
                    .firstName("Ava")
                    .lastName("Admin")
                    .workEmail("admin@dayflow.com")
                    .jobPosition("HR Administrator")
                    .department("Human Resources")
                    .dateOfJoining(LocalDate.now())
                    .yearOfJoining(LocalDate.now().getYear())
                    .serialNo(0)
                    .status(EmployeeStatus.ACTIVE)
                    .build();
            employee = employeeRepository.save(employee);

            resumeRepository.save(Resume.builder().employeeId(employee.getId()).build());
            bankDetailRepository.save(BankDetail.builder().employeeId(employee.getId()).build());

            AppUser admin = AppUser.builder()
                    .loginId("admin")
                    .email("admin@dayflow.com")
                    .passwordHash(passwordEncoder.encode("Admin@123!"))
                    .role(Role.ADMIN)
                    .mustChangePassword(false)
                    .employeeId(employee.getId())
                    .build();
            appUserRepository.save(admin);
        }
    }
}
