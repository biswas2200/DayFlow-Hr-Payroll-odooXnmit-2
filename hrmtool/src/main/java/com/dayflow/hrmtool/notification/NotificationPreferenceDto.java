package com.dayflow.hrmtool.notification;

import lombok.Data;

@Data
public class NotificationPreferenceDto {
    private NotificationType type;
    private boolean emailEnabled;
    private boolean pushEnabled;
    private boolean smsEnabled;
}
