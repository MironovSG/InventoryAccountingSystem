package com.systemtmc.inventory.model.enums;

/**
 * Типы складов
 */
public enum WarehouseType {
    /** Склад расходных материалов */
    CONSUMABLES("Склад расходных материалов"),
    
    /** Склад запасных частей */
    SPARE_PARTS("Склад запасных частей"),
    
    /** Склад комплектующих */
    COMPONENTS("Склад комплектующих");
    
    private final String displayName;
    
    WarehouseType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
