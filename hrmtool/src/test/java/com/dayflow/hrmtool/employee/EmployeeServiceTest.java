package com.dayflow.hrmtool.employee;
import org.springframework.test.util.ReflectionTestUtils;

import com.dayflow.hrmtool.attendance.AttendanceRepository;
import com.dayflow.hrmtool.auth.AppUser;
import com.dayflow.hrmtool.auth.AppUserRepository;
import com.dayflow.hrmtool.auth.LoginIdGenerator;
import com.dayflow.hrmtool.auth.LoginSerialCounter;
import com.dayflow.hrmtool.auth.LoginSerialCounterRepository;
import com.dayflow.hrmtool.auth.TempPasswordGenerator;
import com.dayflow.hrmtool.company.Company;
import com.dayflow.hrmtool.company.CompanyRepository;
import com.dayflow.hrmtool.employee.dto.CreateEmployeeRequest;
import com.dayflow.hrmtool.employee.dto.EmployeeResponse;
import com.dayflow.hrmtool.leave.LeaveRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private ResumeRepository resumeRepository;
    @Mock private BankDetailRepository bankDetailRepository;
    @Mock private AppUserRepository appUserRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private LoginIdGenerator loginIdGenerator;
    @Mock private TempPasswordGenerator tempPasswordGenerator;
    @Mock private LoginSerialCounterRepository loginSerialCounterRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AttendanceRepository attendanceRepository;
    @Mock private LeaveRequestRepository leaveRequestRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private CreateEmployeeRequest validRequest;
    private Company company;

    @BeforeEach
    void setUp() {
        validRequest = new CreateEmployeeRequest();
        validRequest.setFirstName("John");
        validRequest.setLastName("Doe");
        validRequest.setEmail("john.doe@test.com");
        validRequest.setPhone("1234567890");
        validRequest.setPassword("Pass123!");
        validRequest.setConfirmPassword("Pass123!");
        
        company = new Company();
        ReflectionTestUtils.setField(company, "id", 1L);
        company.setInitials("ACME");
    }

    @Test
    void testCreateEmployee_Success() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        
        LoginSerialCounter counter = new LoginSerialCounter();
        counter.setCounter(1);
        when(loginSerialCounterRepository.findByCompanyIdAndYear(anyLong(), anyInt())).thenReturn(Optional.of(counter));
        when(loginIdGenerator.generate(any(), any(), any(), anyInt(), anyInt())).thenReturn("ACME2026001");
        when(tempPasswordGenerator.generate()).thenReturn("TempPass1!");
        when(passwordEncoder.encode(any())).thenReturn("hashedPass");
        
        Employee savedEmp = new Employee();
        ReflectionTestUtils.setField(savedEmp, "id", 100L);
        when(employeeRepository.save(any())).thenReturn(savedEmp);
        
        EmployeeResponse response = employeeService.createEmployee(validRequest, 1L, 999L);
        
        assertNotNull(response);
        assertEquals("ACME2026001", response.getLoginId());
        assertEquals("TempPass1!", response.getTempPassword());
        
        verify(employeeRepository, times(1)).save(any());
        verify(appUserRepository, times(1)).save(any());
        verify(resumeRepository, times(1)).save(any());
        verify(bankDetailRepository, times(1)).save(any());
    }

    @Test
    void testCreateEmployee_PasswordMismatch_ThrowsException() {
        validRequest.setConfirmPassword("Mismatch123!");
        
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(validRequest, 1L, 999L);
        });
        
        assertEquals("Passwords do not match", ex.getMessage());
        verify(employeeRepository, never()).save(any());
    }
}
