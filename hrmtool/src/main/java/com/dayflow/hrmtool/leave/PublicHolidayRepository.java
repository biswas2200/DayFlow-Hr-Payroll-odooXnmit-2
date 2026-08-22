package com.dayflow.hrmtool.leave;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface PublicHolidayRepository extends JpaRepository<PublicHoliday, Long> {
    List<PublicHoliday> findByDateBetween(LocalDate startDate, LocalDate endDate);
}
