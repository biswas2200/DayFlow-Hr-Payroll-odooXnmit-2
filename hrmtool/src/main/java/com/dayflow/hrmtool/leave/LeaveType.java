package com.dayflow.hrmtool.leave;

import com.dayflow.hrmtool.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "leave_type", uniqueConstraints = {@UniqueConstraint(columnNames = {"company_id", "name"})})
public class LeaveType extends BaseEntity {
    private Long companyId;
    private String name;
    private boolean requiresAttachment;
    private boolean isPaid;
}
