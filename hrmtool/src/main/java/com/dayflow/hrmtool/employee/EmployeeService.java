package com.dayflow.hrmtool.employee;

import com.dayflow.hrmtool.attendance.Attendance;
import com.dayflow.hrmtool.attendance.AttendanceRepository;
import com.dayflow.hrmtool.attendance.AttendanceStatus;
import com.dayflow.hrmtool.auth.AppUser;
import com.dayflow.hrmtool.auth.AppUserRepository;
import com.dayflow.hrmtool.auth.LoginIdGenerator;
import com.dayflow.hrmtool.auth.LoginSerialCounter;
import com.dayflow.hrmtool.auth.LoginSerialCounterRepository;
import com.dayflow.hrmtool.auth.Role;
import com.dayflow.hrmtool.auth.TempPasswordGenerator;
import com.dayflow.hrmtool.company.Company;
import com.dayflow.hrmtool.company.CompanyRepository;
import com.dayflow.hrmtool.employee.dto.*;
import com.dayflow.hrmtool.leave.LeaveRequest;
import com.dayflow.hrmtool.leave.LeaveRequestRepository;
import com.dayflow.hrmtool.leave.LeaveStatus;
import com.dayflow.hrmtool.notification.NotificationService;
import com.dayflow.hrmtool.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final ResumeRepository resumeRepository;
    private final BankDetailRepository bankDetailRepository;
    
    private final AppUserRepository appUserRepository;
    private final CompanyRepository companyRepository;
    private final LoginIdGenerator loginIdGenerator;
    private final TempPasswordGenerator tempPasswordGenerator;
    private final LoginSerialCounterRepository loginSerialCounterRepository;
    private final PasswordEncoder passwordEncoder;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final NotificationService notificationService;
    
    public EmployeeResponse createEmployee(CreateEmployeeRequest req, Long companyId, Long creatingUserId) {
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        Company company = resolveCompany(req, companyId);

        int year = LocalDate.now().getYear();
        
        LoginSerialCounter counter = loginSerialCounterRepository.findByCompanyIdAndYear(company.getId(), year)
            .orElse(LoginSerialCounter.builder().companyId(company.getId()).year(year).counter(0).build());

        counter.setCounter(counter.getCounter() + 1);
        loginSerialCounterRepository.save(counter);

        String loginId = loginIdGenerator.generate(company.getInitials(), req.getFirstName(), req.getLastName(), year, counter.getCounter());
        String tempPassword = tempPasswordGenerator.generate();

        Employee employee = Employee.builder()
            .companyId(company.getId())
            .firstName(req.getFirstName())
            .lastName(req.getLastName())
            .workEmail(req.getEmail())
            .phone(req.getPhone())
            .jobPosition(req.getJobPosition())
            .department(req.getDepartment())
            .yearOfJoining(year)
            .serialNo(counter.getCounter())
            .dateOfJoining(LocalDate.now())
            .status(EmployeeStatus.ACTIVE)
            .build();
            
        employee = employeeRepository.save(employee);
        
        AppUser appUser = AppUser.builder()
            .loginId(loginId)
            .email(req.getEmail())
            .passwordHash(passwordEncoder.encode(tempPassword))
            .role(req.getRole() != null ? req.getRole() : Role.EMPLOYEE)
            .mustChangePassword(true)
            .employeeId(employee.getId())
            .build();
            
        appUserRepository.save(appUser);
        
        Resume resume = Resume.builder()
            .employeeId(employee.getId())
            .build();
        resumeRepository.save(resume);
        
        BankDetail bankDetail = BankDetail.builder()
            .employeeId(employee.getId())
            .build();
        bankDetailRepository.save(bankDetail);

        notificationService.notify(
            appUser.getId(),
            "Welcome to DayFlow HRMS",
            "Your account has been created. Login ID: " + loginId + " | Temporary Password: " + tempPassword
                + ". You will be required to change this password on first login.",
            NotificationType.INFO,
            "EMPLOYEE",
            employee.getId()
        );

        return EmployeeResponse.builder()
                .id(employee.getId())
                .loginId(loginId)
                .tempPasswordIssued(true)
                .tempPassword(tempPassword)
                .build();
    }
    
    private Company resolveCompany(CreateEmployeeRequest req, Long fallbackCompanyId) {
        String name = req.getCompanyName();
        if (name != null && !name.trim().isEmpty()) {
            return companyRepository.findByNameIgnoreCase(name.trim())
                .orElseGet(() -> {
                    Company created = Company.builder()
                        .name(name.trim())
                        .logoUrl(req.getCompanyLogoUrl())
                        .initials(deriveInitials(name.trim()))
                        .workingDaysPerWeek(5)
                        .breakHours(java.math.BigDecimal.ONE)
                        .build();
                    return companyRepository.save(created);
                });
        }
        return companyRepository.findById(fallbackCompanyId)
            .orElseThrow(() -> new IllegalArgumentException("Company not found"));
    }

    private String deriveInitials(String companyName) {
        String[] words = companyName.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                initials.append(Character.toUpperCase(word.charAt(0)));
            }
            if (initials.length() >= 3) break;
        }
        if (initials.length() < 2 && !words[0].isEmpty()) {
            initials = new StringBuilder(words[0].toUpperCase());
            if (initials.length() > 2) initials.setLength(2);
        }
        return initials.toString();
    }

    public Long getCompanyIdForEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new com.dayflow.hrmtool.common.ResourceNotFoundException("Employee not found"))
                .getCompanyId();
    }

    public Page<EmployeeCardDto> getDirectory(Long companyId, String search, Pageable pageable) {
        Page<Employee> page;
        if (search != null && !search.trim().isEmpty()) {
            page = employeeRepository.searchDirectory(companyId, search, pageable);
        } else {
            page = employeeRepository.findByCompanyId(companyId, pageable);
        }
        
        return page.map(e -> EmployeeCardDto.builder()
            .id(e.getId())
            .name(e.getFirstName() + " " + e.getLastName())
            .profilePictureUrl(e.getProfilePictureUrl())
            .statusDot(computeStatusDot(e.getId()))
            .build());
    }
    
    public EmployeeProfileDto getById(Long id, Long viewerId, Role viewerRole) {
        Employee e = employeeRepository.findById(id).orElseThrow();
        boolean isAdmin = viewerRole == Role.ADMIN;
        return mapToProfileDto(e, isAdmin || id.equals(viewerId), isAdmin);
    }

    public EmployeeProfileDto getMyProfile(Long userId) {
        AppUser user = appUserRepository.findById(userId).orElseThrow();
        if (user.getEmployeeId() == null) {
            throw new com.dayflow.hrmtool.common.ResourceNotFoundException("Admin has no associated Employee profile");
        }
        Employee e = employeeRepository.findById(user.getEmployeeId())
            .orElseThrow(() -> new com.dayflow.hrmtool.common.ResourceNotFoundException("Employee profile not found"));
        return mapToProfileDto(e, true, user.getRole() == Role.ADMIN);
    }

    public EmployeeProfileDto updateOwnProfile(Long userId, EmployeeSelfEditRequest dto) {
        AppUser user = appUserRepository.findById(userId).orElseThrow();
        if (user.getEmployeeId() == null) {
            throw new com.dayflow.hrmtool.common.ResourceNotFoundException("Admin has no associated Employee profile");
        }
        Employee e = employeeRepository.findById(user.getEmployeeId())
            .orElseThrow(() -> new com.dayflow.hrmtool.common.ResourceNotFoundException("Employee profile not found"));

        if (dto.getPhone() != null) e.setPhone(dto.getPhone());
        if (dto.getResidingAddress() != null) e.setResidingAddress(dto.getResidingAddress());
        if (dto.getProfilePictureUrl() != null) e.setProfilePictureUrl(dto.getProfilePictureUrl());

        employeeRepository.save(e);
        return mapToProfileDto(e, true, user.getRole() == Role.ADMIN);
    }

    public EmployeeProfileDto updateAsAdmin(Long id, EmployeeAdminEditRequest dto) {
        Employee e = employeeRepository.findById(id).orElseThrow();

        if (dto.getPhone() != null) e.setPhone(dto.getPhone());
        if (dto.getResidingAddress() != null) e.setResidingAddress(dto.getResidingAddress());
        if (dto.getProfilePictureUrl() != null) e.setProfilePictureUrl(dto.getProfilePictureUrl());
        if (dto.getJobPosition() != null) e.setJobPosition(dto.getJobPosition());
        if (dto.getDepartment() != null) e.setDepartment(dto.getDepartment());
        if (dto.getManagerId() != null) e.setManagerId(dto.getManagerId());
        if (dto.getStatus() != null) e.setStatus(dto.getStatus());

        employeeRepository.save(e);
        return mapToProfileDto(e, true, true);
    }
    
    public void updateStatus(Long id, EmployeeStatus status) {
        Employee e = employeeRepository.findById(id).orElseThrow();
        e.setStatus(status);
        employeeRepository.save(e);
    }
    
    public StatusDot computeStatusDot(Long employeeId) {
        LocalDate today = LocalDate.now();
        
        var attendanceOpt = attendanceRepository.findByEmployeeIdAndDate(employeeId, today);
        if (attendanceOpt.isPresent() && 
            (attendanceOpt.get().getStatus() == AttendanceStatus.PRESENT || 
             attendanceOpt.get().getStatus() == AttendanceStatus.HALF_DAY)) {
            return StatusDot.GREEN;
        }
        
        List<LeaveRequest> leaves = leaveRequestRepository.findByEmployeeId(employeeId);
        for (LeaveRequest leave : leaves) {
            if (leave.getStatus() == LeaveStatus.APPROVED) {
                if (!today.isBefore(leave.getStartDate()) && !today.isAfter(leave.getEndDate())) {
                    return StatusDot.AIRPLANE;
                }
            }
        }
        
        return StatusDot.YELLOW;
    }
    
    private EmployeeProfileDto mapToProfileDto(Employee e, boolean includePrivate, boolean isAdmin) {
        String loginId = appUserRepository.findByEmployeeId(e.getId())
            .map(AppUser::getLoginId)
            .orElse(null);

        String managerName = null;
        if (e.getManagerId() != null) {
            managerName = employeeRepository.findById(e.getManagerId())
                .map(m -> m.getFirstName() + " " + m.getLastName())
                .orElse(null);
        }

        EmployeeProfileDto.EmployeeProfileDtoBuilder builder = EmployeeProfileDto.builder()
            .id(e.getId())
            .loginId(loginId)
            .firstName(e.getFirstName())
            .lastName(e.getLastName())
            .jobPosition(e.getJobPosition())
            .department(e.getDepartment())
            .manager(managerName)
            .location(e.getLocation())
            .email(e.getWorkEmail())
            .mobile(e.getPhone())
            .profilePictureUrl(e.getProfilePictureUrl())
            .status(e.getStatus())
            .salaryVisible(includePrivate)
            .salaryEditable(isAdmin);

        Resume resume = resumeRepository.findById(e.getId()).orElse(null);
        if (resume != null) {
            builder.resume(ResumeDto.builder()
                .about(resume.getAbout())
                .whatILoveAboutMyJob(resume.getWhatILoveAboutJob())
                .interestsAndHobbies(resume.getInterestsAndHobbies())
                .build());
        }

        if (includePrivate) {
            BankDetail bd = bankDetailRepository.findById(e.getId()).orElse(null);
            BankDetailDto bdDto = null;
            if (bd != null) {
                bdDto = BankDetailDto.builder()
                    .bankName(bd.getBankName())
                    .accountNumber(bd.getAccountNumber())
                    .ifscCode(bd.getIfscCode())
                    .panNo(bd.getPanNo())
                    .uanNo(bd.getUanNo())
                    .empCode(bd.getEmpCode())
                    .build();
            }

            builder.privateInfo(PrivateInfoDto.builder()
                .dateOfBirth(e.getDateOfBirth())
                .residingAddress(e.getResidingAddress())
                .nationality(e.getNationality())
                .personalEmail(e.getPersonalEmail())
                .gender(e.getGender() != null ? e.getGender().name() : null)
                .maritalStatus(e.getMaritalStatus() != null ? e.getMaritalStatus().name() : null)
                .dateOfJoining(e.getDateOfJoining())
                .bankDetails(bdDto)
                .build());
        }

        return builder.build();
    }
}
