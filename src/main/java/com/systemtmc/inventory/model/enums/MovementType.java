package com.systemtmc.inventory.model.enums;

/**
 * Типы движения ТМЦ
 */
public enum MovementType {
    /** Поступление */
    RECEIPT("Поступление"),
    
    /** Выдача */
    ISSUANCE("Выдача"),
    
    /** Списание */
    WRITE_OFF("Списание"),
    
    /** Возврат */
    RETURN("Возврат"),
    
    /** Инвентаризация */
    INVENTORY("Инвентаризация"),
    
    /** Корректировка */
    ADJUSTMENT("Корректировка");
    
    private final String displayName;
    
    MovementType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
