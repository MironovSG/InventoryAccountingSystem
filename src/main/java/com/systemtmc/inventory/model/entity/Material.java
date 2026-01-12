package com.systemtmc.inventory.model.entity;

import com.systemtmc.inventory.model.enums.StockLevel;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Сущность товарно-материальной ценности (ТМЦ)
 */
@Entity
@Table(name = "materials", indexes = {
    @Index(name = "idx_material_article", columnList = "article"),
    @Index(name = "idx_material_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Material extends BaseEntity {
    
    @NotBlank(message = "Артикул обязателен")
    @Size(max = 50, message = "Артикул не может быть длиннее 50 символов")
    @Column(name = "article", unique = true, nullable = false, length = 50)
    private String article;
    
    @NotBlank(message = "Название обязательно")
    @Size(max = 300, message = "Название не может быть длиннее 300 символов")
    @Column(name = "name", nullable = false, length = 300)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private MaterialCategory category;
    
    @NotBlank(message = "Единица измерения обязательна")
    @Size(max = 20, message = "Единица измерения не может быть длиннее 20 символов")
    @Column(name = "unit_of_measure", nullable = false, length = 20)
    private String unitOfMeasure;
    
    @NotNull(message = "Текущий остаток обязателен")
    @Min(value = 0, message = "Текущий остаток не может быть отрицательным")
    @Column(name = "current_quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal currentQuantity = BigDecimal.ZERO;
    
    @Min(value = 0, message = "Минимальный остаток не может быть отрицательным")
    @Column(name = "min_quantity", precision = 15, scale = 3)
    private BigDecimal minQuantity;
    
    @Min(value = 0, message = "Максимальный остаток не может быть отрицательным")
    @Column(name = "max_quantity", precision = 15, scale = 3)
    private BigDecimal maxQuantity;
    
    @Min(value = 0, message = "Критический остаток не может быть отрицательным")
    @Column(name = "critical_quantity", precision = 15, scale = 3)
    private BigDecimal criticalQuantity;
    
    @Column(name = "storage_location", length = 200)
    private String storageLocation;
    
    @Column(name = "warehouse_type", length = 50)
    private String warehouseType; // Тип склада: CONSUMABLES, SPARE_PARTS, COMPONENTS
    
    @Column(name = "storage_conditions", columnDefinition = "TEXT")
    private String storageConditions;
    
    @Min(value = 0, message = "Цена не может быть отрицательной")
    @Column(name = "unit_price", precision = 15, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(name = "manufacturer", length = 200)
    private String manufacturer;
    
    @Column(name = "supplier", length = 200)
    private String supplier;
    
    @Column(name = "barcode", length = 100)
    private String barcode;
    
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    @Column(name = "active", nullable = false)
    private Boolean active = true;
    
    /**
     * Вычислить уровень запаса для визуальной индикации
     */
    public StockLevel calculateStockLevel() {
        if (currentQuantity.compareTo(BigDecimal.ZERO) == 0) {
            return StockLevel.OUT_OF_STOCK;
        }
        
        if (criticalQuantity != null && currentQuantity.compareTo(criticalQuantity) <= 0) {
            return StockLevel.OUT_OF_STOCK;
        }
        
        if (minQuantity != null && currentQuantity.compareTo(minQuantity) <= 0) {
            return StockLevel.LOW_STOCK;
        }
        
        return StockLevel.IN_STOCK;
    }
    
    /**
     * Проверить, достаточно ли материала
     */
    public boolean hasEnough(BigDecimal quantity) {
        return currentQuantity.compareTo(quantity) >= 0;
    }
}
