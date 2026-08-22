package com.dayflow.hrmtool.leave;

import com.dayflow.hrmtool.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "public_holiday")
public class PublicHoliday extends BaseEntity {
    private LocalDate date;
    private String name;
}
