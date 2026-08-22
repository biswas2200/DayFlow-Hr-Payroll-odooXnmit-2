package com.dayflow.hrmtool.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.Instant;

@Data
public class NotificationDto {
    private Long id;
    private Long userId;
    private String title;
    private String message;
    private NotificationType type;
    /** Serialized as "read" to match the Angular frontend model. */
    @JsonProperty("read")
    private boolean readStatus;
    private String refType;
    private Long refId;
    private Instant createdAt;
}

