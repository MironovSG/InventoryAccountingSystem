package com.systemtmc.inventory.model.enums;

/**
 * Перечисление ролей пользователей в системе
 */
public enum UserRole {
    /** Материально-ответственное лицо */
    MOL("Материально-ответственное лицо"),
    
    /** Инженер сервисного центра */
    ENGINEER("Инженер"),
    
    /** Специалист АРМ */
    SPECIALIST_ARM("Специалист АРМ"),
    
    /** Специалист ГОТО */
    SPECIALIST_GOTO("Специалист ГОТО"),
    
    /** Специалист ГОКС */
    SPECIALIST_GOKS("Специалист ГОКС"),
    
    /** Руководитель подразделения */
    MANAGER("Руководитель"),
    
    /** ИТ-администратор */
    ADMIN("Администратор");
    
    private final String displayName;
    
    UserRole(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
