package com.systemtmc.inventory.model.enums;

/**
 * Рабочие группы специалистов
 */
public enum WorkGroup {
    /** Группа АРМ */
    ARM("АРМ"),
    
    /** Группа ГОТО */
    GOTO("ГОТО"),
    
    /** Группа ГОКС */
    GOKS("ГОКС");
    
    private final String displayName;
    
    WorkGroup(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
