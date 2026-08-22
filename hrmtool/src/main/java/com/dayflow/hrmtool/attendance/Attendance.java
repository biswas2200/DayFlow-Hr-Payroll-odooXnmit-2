package com.dayflow.hrmtool.attendance;

import com.dayflow.hrmtool.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "attendance", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"employee_id", "date"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance extends BaseEntity {
    
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;
    
    @Column(nullable = false)
    private LocalDate date;
    
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    
    private Long workHours;
    private Long extraHours;
    private Long breakDuration;
    
    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;
}
