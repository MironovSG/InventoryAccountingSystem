package com.systemtmc.inventory.dto;

import com.systemtmc.inventory.model.enums.StockLevel;
import com.systemtmc.inventory.model.enums.WarehouseType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO для ТМЦ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialDTO {
    private Long id;
    
    @NotBlank(message = "Артикул обязателен")
    @Size(max = 50)
    private String article;
    
    @NotBlank(message = "Название обязательно")
    @Size(max = 300)
    private String name;
    
    private String description;
    
    @NotNull(message = "Категория обязательна")
    private Long categoryId;
    
    private String categoryName;
    
    @NotBlank(message = "Единица измерения обязательна")
    @Size(max = 20)
    private String unitOfMeasure;
    
    @NotNull
    @Min(0)
    private BigDecimal currentQuantity;
    
    @Min(0)
    private BigDecimal minQuantity;
    
    @Min(0)
    private BigDecimal maxQuantity;
    
    @Min(0)
    private BigDecimal criticalQuantity;
    
    private String storageLocation;
    private String storageConditions;
    
    @Min(0)
    private BigDecimal unitPrice;
    
    private String manufacturer;
    private String supplier;
    private String barcode;
    private String imageUrl;
    private Boolean active;
    private StockLevel stockLevel;
    private WarehouseType warehouseType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
