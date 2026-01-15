package com.systemtmc.inventory.model.entity;

import com.systemtmc.inventory.model.enums.RequestStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Сущность заявки на выдачу ТМЦ
 */
@Entity
@Table(name = "requests", indexes = {
    @Index(name = "idx_request_number", columnList = "request_number"),
    @Index(name = "idx_request_status", columnList = "status"),
    @Index(name = "idx_request_requester", columnList = "requester_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Request extends BaseEntity {
    
    @NotBlank(message = "Номер заявки обязателен")
    @Column(name = "request_number", unique = true, nullable = false, length = 50)
    private String requestNumber;
    
    @NotNull(message = "Статус заявки обязателен")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RequestStatus status;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;
    
    @NotBlank(message = "Назначение обязательно")
    @Size(max = 500, message = "Назначение не может быть длиннее 500 символов")
    @Column(name = "purpose", nullable = false, length = 500)
    private String purpose;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RequestItem> items = new ArrayList<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private User approvedBy;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issued_by_id")
    private User issuedBy;
    
    @Column(name = "issued_at")
    private LocalDateTime issuedAt;
    
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
    
    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 0;
    
    @Column(name = "expected_date")
    private LocalDateTime expectedDate;
    
    /**
     * Добавить элемент к заявке
     */
    public void addItem(RequestItem item) {
        items.add(item);
        item.setRequest(this);
    }
    
    /**
     * Удалить элемент из заявки
     */
    public void removeItem(RequestItem item) {
        items.remove(item);
        item.setRequest(null);
    }
}
