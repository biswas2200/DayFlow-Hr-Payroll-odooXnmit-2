package com.dayflow.hrmtool.leave;

import com.dayflow.hrmtool.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "leave_request")
public class LeaveRequest extends BaseEntity {
    private Long employeeId;
    private Long leaveTypeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal numDays;
    private String attachmentUrl;
    
    @Enumerated(EnumType.STRING)
    private LeaveStatus status;
    
    private Long approverId;
    private String approverComment;
    private Instant decidedAt;
}
