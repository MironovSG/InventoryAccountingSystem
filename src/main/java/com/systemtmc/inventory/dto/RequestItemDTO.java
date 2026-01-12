package com.systemtmc.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO для позиции заявки
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestItemDTO {
    private Long id;
    
    @NotNull(message = "Материал обязателен")
    private Long materialId;
    
    private String materialName;
    private String materialArticle;
    private String unitOfMeasure;
    
    @NotNull(message = "Запрошенное количество обязательно")
    @DecimalMin(value = "0.001", message = "Количество должно быть больше 0")
    private BigDecimal requestedQuantity;
    
    private BigDecimal approvedQuantity;
    private BigDecimal issuedQuantity;
    private BigDecimal availableQuantity;
    private String notes;
}
