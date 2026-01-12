package com.systemtmc.inventory.service;

import com.systemtmc.inventory.model.entity.AuditLog;
import com.systemtmc.inventory.model.entity.User;
import com.systemtmc.inventory.repository.AuditLogRepository;
import com.systemtmc.inventory.repository.UserRepository;
import com.systemtmc.inventory.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис для аудита действий пользователей
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {
    
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public void logAction(String action, String entityType, Long entityId, String description) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .description(description)
                    .build();
            
            if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
                User user = userRepository.findById(userPrincipal.getId()).orElse(null);
                
                auditLog.setUser(user);
                auditLog.setUsername(userPrincipal.getUsername());
            }
            
            auditLogRepository.save(auditLog);
            log.debug("Зафиксировано действие: {} - {}", action, description);
        } catch (Exception e) {
            log.error("Ошибка при записи в аудит лог", e);
        }
    }
    
    @Transactional
    public void logAction(String action, String entityType, Long entityId, 
                         String description, String oldValue, String newValue) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .description(description)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .build();
            
            if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
                User user = userRepository.findById(userPrincipal.getId()).orElse(null);
                
                auditLog.setUser(user);
                auditLog.setUsername(userPrincipal.getUsername());
            }
            
            auditLogRepository.save(auditLog);
            log.debug("Зафиксировано действие: {} - {}", action, description);
        } catch (Exception e) {
            log.error("Ошибка при записи в аудит лог", e);
        }
    }
    
    @Transactional(readOnly = true)
    public List<AuditLog> getLogsByUser(Long userId) {
        return auditLogRepository.findLogsByUser(userId);
    }
    
    @Transactional(readOnly = true)
    public List<AuditLog> getLogsByEntity(String entityType, Long entityId) {
        return auditLogRepository.findLogsByEntity(entityType, entityId);
    }
    
    @Transactional(readOnly = true)
    public List<AuditLog> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findLogsByDateRange(startDate, endDate);
    }
}
