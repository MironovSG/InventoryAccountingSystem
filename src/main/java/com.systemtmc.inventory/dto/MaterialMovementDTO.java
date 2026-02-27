package com.systemtmc.inventory.dto;

import com.systemtmc.inventory.model.enums.MovementType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO для движения ТМЦ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialMovementDTO {
    private Long id;
    
    @NotNull(message = "Материал обязателен")
    private Long materialId;
    
    private String materialName;
    private String materialArticle;
    
    @NotNull(message = "Тип движения обязателен")
    private MovementType movementType;
    
    @NotNull(message = "Количество обязательно")
    @DecimalMin(value = "0.001")
    private BigDecimal quantity;
    
    private BigDecimal quantityBefore;
    private BigDecimal quantityAfter;
    
    private Long requestId;
    private String requestNumber;
    
    private Long userId;
    private String userName;
    
    private Long departmentId;
    private String departmentName;
    
    private String documentNumber;
    private String notes;
    
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    
    private LocalDateTime createdAt;
}
