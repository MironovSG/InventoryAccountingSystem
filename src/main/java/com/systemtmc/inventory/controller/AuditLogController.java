package com.systemtmc.inventory.controller;

import com.systemtmc.inventory.model.entity.AuditLog;
import com.systemtmc.inventory.repository.AuditLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Контроллер для работы с журналом аудита
 */
@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Audit Logs", description = "API для работы с журналом аудита")
public class AuditLogController {
    
    private final AuditLogRepository auditLogRepository;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить все записи журнала")
    public ResponseEntity<List<AuditLog>> getAllLogs() {
        return ResponseEntity.ok(auditLogRepository.findAll());
    }
    
    @GetMapping("/recent")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить последние записи")
    public ResponseEntity<List<AuditLog>> getRecentLogs(
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(auditLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit)));
    }
    
    @GetMapping("/by-date-range")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить записи за период")
    public ResponseEntity<List<AuditLog>> getLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        LocalDateTime start = LocalDateTime.of(startDate, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(endDate, LocalTime.MAX);
        
        return ResponseEntity.ok(auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end));
    }
    
    @GetMapping("/by-user/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить записи по пользователю")
    public ResponseEntity<List<AuditLog>> getLogsByUser(@PathVariable String username) {
        return ResponseEntity.ok(auditLogRepository.findByUsernameOrderByCreatedAtDesc(username));
    }
    
    @GetMapping("/by-action/{action}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить записи по действию")
    public ResponseEntity<List<AuditLog>> getLogsByAction(@PathVariable String action) {
        return ResponseEntity.ok(auditLogRepository.findByActionContainingOrderByCreatedAtDesc(action));
    }
    
    @GetMapping("/by-entity/{entityType}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить записи по типу объекта")
    public ResponseEntity<List<AuditLog>> getLogsByEntityType(@PathVariable String entityType) {
        return ResponseEntity.ok(auditLogRepository.findByEntityTypeOrderByCreatedAtDesc(entityType));
    }
    
    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить статистику по логам")
    public ResponseEntity<Map<String, Long>> getLogsCount() {
        Map<String, Long> stats = new HashMap<>();
        
        // Всего записей
        stats.put("total", auditLogRepository.count());
        
        // За сегодня
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        stats.put("today", (long) auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startOfDay, endOfDay).size());
        
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/export/pdf")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Экспорт логов в PDF")
    public ResponseEntity<byte[]> exportToPDF(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            List<AuditLog> logs;
            
            if (startDate != null && endDate != null) {
                LocalDateTime start = LocalDateTime.of(startDate, LocalTime.MIN);
                LocalDateTime end = LocalDateTime.of(endDate, LocalTime.MAX);
                logs = auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
            } else {
                logs = auditLogRepository.findAll();
            }
            
            // Простая генерация PDF (заглушка - в реальной системе использовать библиотеку типа iText)
            StringBuilder pdf = new StringBuilder();
            pdf.append("ЖУРНАЛ АУДИТА СИСТЕМЫ\n");
            pdf.append("===================\n\n");
            
            if (startDate != null && endDate != null) {
                pdf.append("Период: ").append(startDate).append(" - ").append(endDate).append("\n\n");
            }
            
            for (AuditLog log : logs) {
                pdf.append(log.getCreatedAt()).append(" | ");
                pdf.append(log.getUsername() != null ? log.getUsername() : "Система").append(" | ");
                pdf.append(log.getAction()).append(" | ");
                pdf.append(log.getEntityType() != null ? log.getEntityType() : "-").append(" | ");
                pdf.append(log.getDescription() != null ? log.getDescription() : "-").append("\n");
            }
            
            byte[] pdfBytes = pdf.toString().getBytes();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "audit-logs.pdf");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
            
        } catch (Exception e) {
            log.error("Error exporting logs to PDF", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/export/excel")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Экспорт логов в Excel")
    public ResponseEntity<byte[]> exportToExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            List<AuditLog> logs;
            
            if (startDate != null && endDate != null) {
                LocalDateTime start = LocalDateTime.of(startDate, LocalTime.MIN);
                LocalDateTime end = LocalDateTime.of(endDate, LocalTime.MAX);
                logs = auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
            } else {
                logs = auditLogRepository.findAll();
            }
            
            // Простая генерация CSV (заглушка - в реальной системе использовать Apache POI)
            StringBuilder csv = new StringBuilder();
            csv.append("ID,Время,Пользователь,Действие,Тип объекта,ID объекта,Описание\n");
            
            for (AuditLog log : logs) {
                csv.append(log.getId()).append(",");
                csv.append(log.getCreatedAt()).append(",");
                csv.append(log.getUsername() != null ? log.getUsername() : "Система").append(",");
                csv.append(log.getAction()).append(",");
                csv.append(log.getEntityType() != null ? log.getEntityType() : "-").append(",");
                csv.append(log.getEntityId() != null ? log.getEntityId() : "-").append(",");
                csv.append(log.getDescription() != null ? "\"" + log.getDescription().replace("\"", "\"\"") + "\"" : "-").append("\n");
            }
            
            byte[] csvBytes = csv.toString().getBytes("UTF-8");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
            headers.setContentDispositionFormData("attachment", "audit-logs.csv");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvBytes);
            
        } catch (Exception e) {
            log.error("Error exporting logs to Excel", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
