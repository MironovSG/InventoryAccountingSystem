package com.systemtmc.inventory.repository;

import com.systemtmc.inventory.model.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Репозиторий для работы с журналом аудита
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    List<AuditLog> findByUserId(Long userId);
    
    List<AuditLog> findByAction(String action);
    
    List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);
    
    @Query("SELECT al FROM AuditLog al WHERE al.user.id = :userId ORDER BY al.createdAt DESC")
    List<AuditLog> findLogsByUser(@Param("userId") Long userId);
    
    @Query("SELECT al FROM AuditLog al WHERE al.action = :action ORDER BY al.createdAt DESC")
    List<AuditLog> findLogsByAction(@Param("action") String action);
    
    @Query("SELECT al FROM AuditLog al WHERE al.entityType = :entityType AND al.entityId = :entityId ORDER BY al.createdAt DESC")
    List<AuditLog> findLogsByEntity(@Param("entityType") String entityType, @Param("entityId") Long entityId);
    
    @Query("SELECT al FROM AuditLog al WHERE al.createdAt BETWEEN :startDate AND :endDate ORDER BY al.createdAt DESC")
    List<AuditLog> findLogsByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT al FROM AuditLog al WHERE al.user.id = :userId AND " +
           "al.createdAt BETWEEN :startDate AND :endDate ORDER BY al.createdAt DESC")
    List<AuditLog> findLogsByUserAndDateRange(
        @Param("userId") Long userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // Дополнительные методы для админа
    List<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    List<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);
    
    List<AuditLog> findByUsernameOrderByCreatedAtDesc(String username);
    
    List<AuditLog> findByActionContainingOrderByCreatedAtDesc(String action);
    
    List<AuditLog> findByEntityTypeOrderByCreatedAtDesc(String entityType);
}
