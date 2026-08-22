package com.dayflow.hrmtool.leave;

import com.dayflow.hrmtool.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "leave_allocation")
public class LeaveAllocation extends BaseEntity {
    private Long employeeId;
    private Long leaveTypeId;
    private int year;
    private BigDecimal allocatedDays;
    private BigDecimal usedDays = BigDecimal.ZERO;
}
