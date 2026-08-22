package com.dayflow.hrmtool.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final NotificationWebSocketHandler webSocketHandler;

    @Transactional
    public void notify(Long userId, NotificationType type, String message, String refType, Long refId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setMessage(message);
        notification.setTitle(type.name());
        notification.setRefType(refType);
        notification.setRefId(refId);
        notification.setReadStatus(false);
        
        notification = notificationRepository.save(notification);
        final Notification savedNotification = notification;
        
        // Push via WebSocket
        try {
            webSocketHandler.sendNotification(userId, mapToDto(savedNotification));
        } catch (Exception e) {
            log.error("Failed to send websocket notification", e);
        }
        
        // Send email async based on preferences
        preferenceRepository.findByUserIdAndType(userId, type)
            .ifPresentOrElse(pref -> {
                if (pref.isEmailEnabled()) {
                    sendEmailAsync(userId, savedNotification);
                }
            }, () -> {
                // default behavior if no pref found (e.g. send email)
                sendEmailAsync(userId, savedNotification);
            });
    }


    private void sendEmailAsync(Long userId, Notification notification) {
        // Implement async email sending
        log.info("Sending email to user {} for notification {}", userId, notification.getId());
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
