package com.dayflow.hrmtool.notification;

import com.dayflow.hrmtool.auth.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/notifications/me")
    public ResponseEntity<List<NotificationDto>> getMyNotifications(
            @AuthenticationPrincipal AppUser currentUser,
            @RequestParam(defaultValue = "false") boolean unreadOnly) {
        return ResponseEntity.ok(notificationService.listMine(currentUser.getId(), unreadOnly));
    }

    @PatchMapping("/notifications/{id}/read")
    public ResponseEntity<Void> markNotificationAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUser currentUser) {
        notificationService.markRead(id, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/notification-preferences/me")
    public ResponseEntity<List<NotificationPreferenceDto>> getMyPreferences(
            @AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.ok(notificationService.getPreferences(currentUser.getId()));
    }

    @PutMapping("/notification-preferences/me")
    public ResponseEntity<Void> updateMyPreferences(
            @AuthenticationPrincipal AppUser currentUser,
            @RequestBody List<NotificationPreferenceDto> dtos) {
        notificationService.updatePreferences(currentUser.getId(), dtos);
        return ResponseEntity.ok().build();
    }
}
