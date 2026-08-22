package com.dayflow.hrmtool.notification;

import lombok.Data;
import java.time.Instant;

@Data
public class NotificationDto {
    private Long id;
    private Long userId;
    private String title;
    private String message;
    private NotificationType type;
    private boolean readStatus;
    private String refType;
    private Long refId;
    private Instant createdAt;
}
