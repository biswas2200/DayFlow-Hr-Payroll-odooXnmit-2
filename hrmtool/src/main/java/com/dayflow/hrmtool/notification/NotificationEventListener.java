package com.dayflow.hrmtool.notification;

import com.dayflow.hrmtool.attendance.Attendance;
import com.dayflow.hrmtool.attendance.AttendanceRepository;
import com.dayflow.hrmtool.auth.AppUser;
import com.dayflow.hrmtool.auth.AppUserRepository;
import com.dayflow.hrmtool.auth.Role;
import com.dayflow.hrmtool.leave.event.LeaveAppliedEvent;
import com.dayflow.hrmtool.leave.event.LeaveDecidedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final AppUserRepository appUserRepository;
    private final AttendanceRepository attendanceRepository;

    @EventListener
    public void onLeaveApplied(LeaveAppliedEvent event) {
        log.info("Received LeaveAppliedEvent for employee {}", event.getEmployeeId());
        List<AppUser> admins = appUserRepository.findByRole(Role.ADMIN);
        
        String message = "New leave request from employee ID: " + event.getEmployeeId();
        
        for (AppUser admin : admins) {
            notificationService.notify(
                    admin.getId(),
                    NotificationType.LEAVE_REQUEST,
                    message,
                    "LeaveRequest",
                    event.getLeaveRequestId()
            );
        }
    }

    @EventListener
    public void onLeaveDecided(LeaveDecidedEvent event) {
        log.info("Received LeaveDecidedEvent for leave request {}", event.getLeaveRequestId());
        appUserRepository.findByEmployeeId(event.getEmployeeId()).ifPresent(user -> {
            String message = "Your leave request has been " + event.getStatus();
            notificationService.notify(
                    user.getId(),
                    NotificationType.LEAVE_DECISION,
                    message,
                    "LeaveRequest",
                    event.getLeaveRequestId()
            );
        });
    }

    @Scheduled(cron = "0 30 19 * * MON-FRI")
    public void checkMissingCheckOuts() {
        log.info("Running scheduled check for missing check-outs");
        LocalDate today = LocalDate.now();
        List<Attendance> attendances = attendanceRepository.findByDateAndCheckInTimeIsNotNullAndCheckOutTimeIsNull(today);
        
        for (Attendance attendance : attendances) {
            appUserRepository.findByEmployeeId(attendance.getEmployeeId()).ifPresent(user -> {
                String message = "You have checked in but not checked out today. Please remember to check out.";
                notificationService.notify(
                        user.getId(),
                        NotificationType.REMINDER,
                        message,
                        "Attendance",
                        attendance.getId()
                );
            });
        }
    }
}
