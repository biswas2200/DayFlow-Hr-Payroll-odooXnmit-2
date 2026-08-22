package com.dayflow.hrmtool.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import com.dayflow.hrmtool.auth.AppUserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final NotificationWebSocketHandler webSocketHandler;
    private final AppUserRepository appUserRepository;
    private final JavaMailSender mailSender;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void notify(Long userId, String title, String message, NotificationType type, String refType, Long refId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRefType(refType);
        notification.setRefId(refId);
        notification.setReadStatus(false);
        
        final Notification savedNotification = notificationRepository.save(notification);

        preferenceRepository.findByUserId(userId)
            .stream()
            .findFirst()
            .ifPresentOrElse(pref -> {
                if (pref.isPushEnabled()) {
                    messagingTemplate.convertAndSend("/topic/user-" + userId, mapToDto(savedNotification));
                }
                if (pref.isEmailEnabled()) {
                    sendEmailAsync(userId, savedNotification);
                }
            }, () -> {
                // default behavior if no pref found (e.g. send email)
                sendEmailAsync(userId, savedNotification);
            });
    }

    @Async
    public void sendEmailAsync(Long userId, Notification notification) {
        try {
            appUserRepository.findById(userId).ifPresent(user -> {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom("noreply@dayflow.local");
                message.setTo(user.getEmail());
                message.setSubject(notification.getTitle());
                message.setText(notification.getMessage());
                mailSender.send(message);
                log.info("Email sent to user {} for notification {}", userId, notification.getId());
            });
        } catch (Exception e) {
            log.error("Failed to send email to user {} for notification {}: {}", userId, notification.getId(), e.getMessage());
        }
    }

    public List<NotificationDto> listMine(Long userId, boolean unreadOnly) {
        List<Notification> notifications;
        if (unreadOnly) {
            notifications = notificationRepository.findByUserIdAndReadStatusFalse(userId);
        } else {
            notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }
        return notifications.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional
    public void markRead(Long id, Long userId) {
        notificationRepository.findById(id).ifPresent(notification -> {
            if (notification.getUserId().equals(userId)) {
                notification.setReadStatus(true);
                notificationRepository.save(notification);
            }
        });
    }

    public List<NotificationPreferenceDto> getPreferences(Long userId) {
        return preferenceRepository.findByUserId(userId).stream()
                .map(this::mapPreferenceToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updatePreferences(Long userId, List<NotificationPreferenceDto> dtos) {
        for (NotificationPreferenceDto dto : dtos) {
            NotificationPreference pref = preferenceRepository.findByUserIdAndType(userId, dto.getType())
                    .orElseGet(() -> {
                        NotificationPreference p = new NotificationPreference();
                        p.setUserId(userId);
                        p.setType(dto.getType());
                        return p;
                    });
            pref.setEmailEnabled(dto.isEmailEnabled());
            pref.setPushEnabled(dto.isPushEnabled());
            pref.setSmsEnabled(dto.isSmsEnabled());
            preferenceRepository.save(pref);
        }
    }

    private NotificationDto mapToDto(Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUserId());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setReadStatus(notification.isReadStatus());
        dto.setRefType(notification.getRefType());
        dto.setRefId(notification.getRefId());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }

    private NotificationPreferenceDto mapPreferenceToDto(NotificationPreference pref) {
        NotificationPreferenceDto dto = new NotificationPreferenceDto();
        dto.setType(pref.getType());
        dto.setEmailEnabled(pref.isEmailEnabled());
        dto.setPushEnabled(pref.isPushEnabled());
        dto.setSmsEnabled(pref.isSmsEnabled());
        return dto;
    }
}
