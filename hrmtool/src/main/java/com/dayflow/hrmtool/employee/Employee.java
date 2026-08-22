package com.dayflow.hrmtool.employee;

import com.dayflow.hrmtool.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "employee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    private String firstName;
    private String lastName;
    private String workEmail;
    private String personalEmail;
    private String phone;
    
    private String jobPosition;
    private String department;
    private String location;

    @Column(name = "manager_id")
    private Long managerId;

    private String profilePictureUrl;

    private LocalDate dateOfBirth;
    private LocalDate dateOfJoining;
    
    private int yearOfJoining;
    private int serialNo;
    
    private String residingAddress;
    private String nationality;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private MaritalStatus maritalStatus;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EmployeeStatus status = EmployeeStatus.ACTIVE;
}
