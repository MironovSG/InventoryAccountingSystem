package com.systemtmc.inventory.model.enums;

/**
 * Уровни запасов для визуальной индикации
 */
public enum StockLevel {
    /** В наличии (зеленый) */
    IN_STOCK("В наличии", "green"),
    
    /** Мало (желтый) */
    LOW_STOCK("Мало", "yellow"),
    
    /** Отсутствует (красный) */
    OUT_OF_STOCK("Отсутствует", "red");
    
    private final String displayName;
    private final String color;
    
    StockLevel(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getColor() {
        return color;
    }
}
