package com.dayflow.hrmtool.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationWebSocketHandler {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendNotification(Long userId, NotificationDto dto) {
        messagingTemplate.convertAndSendToUser(
                String.valueOf(userId), "/queue/notifications", dto
        );
    }
}
