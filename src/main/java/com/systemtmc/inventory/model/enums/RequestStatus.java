package com.systemtmc.inventory.model.enums;

/**
 * Статусы заявок на выдачу ТМЦ
 */
public enum RequestStatus {
    /** Принята */
    ACCEPTED("Принята"),
    
    /** В работе */
    IN_PROGRESS("В работе"),
    
    /** Ожидает выдачи */
    AWAITING_ISSUANCE("Ожидает выдачи"),
    
    /** Выдана */
    ISSUED("Выдана"),
    
    /** Закрыта */
    CLOSED("Закрыта"),
    
    /** Отклонена */
    REJECTED("Отклонена");
    
    private final String displayName;
    
    RequestStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
