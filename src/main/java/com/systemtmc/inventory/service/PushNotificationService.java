package com.systemtmc.inventory.service;

import com.systemtmc.inventory.dto.NotificationDTO;
import com.systemtmc.inventory.model.entity.Notification;
import com.systemtmc.inventory.model.entity.Request;
import com.systemtmc.inventory.model.entity.User;
import com.systemtmc.inventory.repository.NotificationRepository;
import com.systemtmc.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для работы с push-уведомлениями
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationService {
    
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public void sendNotificationToUser(Long userId, String title, String message, String type, 
                                       String relatedEntityType, Long relatedEntityId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)
                .build();
        
        notificationRepository.save(notification);
        log.info("Push-уведомление отправлено пользователю {}: {}", user.getUsername(), title);
    }
    
    @Transactional
    public void sendRequestAcceptedNotification(Request request) {
        sendNotificationToUser(
                request.getRequester().getId(),
                "Заявка принята в работу",
                "Ваша заявка №" + request.getRequestNumber() + " принята в работу МОЛ",
                "REQUEST_IN_PROGRESS",
                "Request",
                request.getId()
        );
    }
    
    @Transactional
    public void sendRequestIssuedNotification(Request request) {
        sendNotificationToUser(
                request.getRequester().getId(),
                "Материалы выданы",
                "По вашей заявке №" + request.getRequestNumber() + " материалы выданы",
                "REQUEST_ISSUED",
                "Request",
                request.getId()
        );
    }
    
    @Transactional
    public void sendRequestRejectedNotification(Request request) {
        sendNotificationToUser(
                request.getRequester().getId(),
                "Заявка отклонена",
                "Ваша заявка №" + request.getRequestNumber() + " отклонена. Причина: " + 
                        (request.getRejectionReason() != null ? request.getRejectionReason() : "не указана"),
                "REQUEST_REJECTED",
                "Request",
                request.getId()
        );
    }
    
    @Transactional
    public void sendNewRequestNotificationToMOL(Request request) {
        // Найти всех пользователей с ролью МОЛ
        List<User> molUsers = userRepository.findActiveUsersByRole(com.systemtmc.inventory.model.enums.UserRole.MOL);
        
        for (User molUser : molUsers) {
            sendNotificationToUser(
                    molUser.getId(),
                    "Новая заявка на ТМЦ",
                    "Поступила новая заявка №" + request.getRequestNumber() + 
                            " от " + request.getRequester().getFullName() + 
                            " (" + request.getDepartment().getName() + ")",
                    "NEW_REQUEST",
                    "Request",
                    request.getId()
            );
        }
        
        log.info("Отправлены уведомления о новой заявке {} всем МОЛ", request.getRequestNumber());
    }
    
    @Transactional(readOnly = true)
    public List<NotificationDTO> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<NotificationDTO> getUnreadNotifications(Long userId) {
        return notificationRepository.findUnreadByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }
    
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Уведомление не найдено"));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }
    
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository.findUnreadByUserId(userId);
        notifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }
    
    private NotificationDTO convertToDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .userId(notification.getUser().getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.getIsRead())
                .relatedEntityType(notification.getRelatedEntityType())
                .relatedEntityId(notification.getRelatedEntityId())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
