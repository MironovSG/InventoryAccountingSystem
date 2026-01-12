package com.systemtmc.inventory.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/**
 * Сущность элемента заявки (позиция в заявке)
 */
@Entity
@Table(name = "request_items", indexes = {
    @Index(name = "idx_request_item_request", columnList = "request_id"),
    @Index(name = "idx_request_item_material", columnList = "material_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestItem extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;
    
    @NotNull(message = "Запрошенное количество обязательно")
    @DecimalMin(value = "0.001", message = "Количество должно быть больше 0")
    @Column(name = "requested_quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal requestedQuantity;
    
    @Column(name = "approved_quantity", precision = 15, scale = 3)
    private BigDecimal approvedQuantity;
    
    @Column(name = "issued_quantity", precision = 15, scale = 3)
    private BigDecimal issuedQuantity;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
