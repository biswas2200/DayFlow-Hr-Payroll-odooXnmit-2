package com.dayflow.hrmtool.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class NotificationPreferenceDto {
    private NotificationType type;
    private boolean emailEnabled;
    /** Serialized as "inAppEnabled" to match the Angular frontend NotificationPreference model. */
    @JsonProperty("inAppEnabled")
    private boolean pushEnabled;
    private boolean smsEnabled;
}

