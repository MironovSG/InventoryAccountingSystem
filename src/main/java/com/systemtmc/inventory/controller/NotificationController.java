package com.systemtmc.inventory.controller;

import com.systemtmc.inventory.dto.NotificationDTO;
import com.systemtmc.inventory.security.UserPrincipal;
import com.systemtmc.inventory.service.PushNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для работы с уведомлениями
 */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "API для работы с уведомлениями")
public class NotificationController {
    
    private final PushNotificationService notificationService;
    
    @GetMapping
    @Operation(summary = "Получить все уведомления текущего пользователя")
    public ResponseEntity<List<NotificationDTO>> getMyNotifications(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userPrincipal.getId()));
    }
    
    @GetMapping("/unread")
    @Operation(summary = "Получить непрочитанные уведомления")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(userPrincipal.getId()));
    }
    
    @GetMapping("/unread/count")
    @Operation(summary = "Получить количество непрочитанных уведомлений")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userPrincipal.getId()));
    }
    
    @PutMapping("/{id}/read")
    @Operation(summary = "Отметить уведомление как прочитанное")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/read-all")
    @Operation(summary = "Отметить все уведомления как прочитанные")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        notificationService.markAllAsRead(userPrincipal.getId());
        return ResponseEntity.ok().build();
    }
}
