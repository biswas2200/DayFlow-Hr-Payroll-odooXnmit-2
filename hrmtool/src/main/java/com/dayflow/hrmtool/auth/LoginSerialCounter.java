package com.dayflow.hrmtool.auth;

import com.dayflow.hrmtool.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

@Entity
@Table(
    name = "login_serial_counter",
    uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "year"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginSerialCounter extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "year", nullable = false)
    private int year;

    @Column(nullable = false)
    private int counter;
}
