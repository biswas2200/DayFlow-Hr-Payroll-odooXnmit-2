package com.dayflow.hrmtool.notification;

import com.dayflow.hrmtool.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "notification_preference")
public class NotificationPreference extends BaseEntity {
    private Long userId;
    
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private NotificationType type;
    
    private boolean emailEnabled;
    private boolean pushEnabled;
    private boolean smsEnabled;
}
