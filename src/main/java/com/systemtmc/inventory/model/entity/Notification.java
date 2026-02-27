package com.systemtmc.inventory.model.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Сущность уведомления для push-уведомлений
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notification_user", columnList = "user_id"),
    @Index(name = "idx_notification_read", columnList = "is_read"),
    @Index(name = "idx_notification_date", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "type", length = 50)
    private String type; // REQUEST_ACCEPTED, REQUEST_IN_PROGRESS, REQUEST_ISSUED, etc.
    
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
    
    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType; // Request, Material, etc.
    
    @Column(name = "related_entity_id")
    private Long relatedEntityId;
}
