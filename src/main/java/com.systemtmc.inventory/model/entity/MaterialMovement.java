package com.systemtmc.inventory.model.entity;

import com.systemtmc.inventory.model.enums.MovementType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/**
 * Сущность движения ТМЦ (история операций)
 */
@Entity
@Table(name = "material_movements", indexes = {
    @Index(name = "idx_movement_material", columnList = "material_id"),
    @Index(name = "idx_movement_type", columnList = "movement_type"),
    @Index(name = "idx_movement_date", columnList = "created_at"),
    @Index(name = "idx_movement_request", columnList = "request_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialMovement extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;
    
    @NotNull(message = "Тип движения обязателен")
    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 20)
    private MovementType movementType;
    
    @NotNull(message = "Количество обязательно")
    @DecimalMin(value = "0.001", message = "Количество должно быть больше 0")
    @Column(name = "quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal quantity;
    
    @Column(name = "quantity_before", precision = 15, scale = 3)
    private BigDecimal quantityBefore;
    
    @Column(name = "quantity_after", precision = 15, scale = 3)
    private BigDecimal quantityAfter;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private Request request;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
    
    @Column(name = "document_number", length = 100)
    private String documentNumber;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "unit_price", precision = 15, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(name = "total_price", precision = 15, scale = 2)
    private BigDecimal totalPrice;
}
