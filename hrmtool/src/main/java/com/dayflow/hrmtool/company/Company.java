package com.dayflow.hrmtool.company;

import com.dayflow.hrmtool.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "company")
public class Company extends BaseEntity {
    private String name;
    private String logoUrl;
    private String initials;
    private int workingDaysPerWeek;
    private BigDecimal breakHours;
}
