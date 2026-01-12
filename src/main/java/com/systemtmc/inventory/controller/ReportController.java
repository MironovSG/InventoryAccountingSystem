package com.systemtmc.inventory.controller;

import com.systemtmc.inventory.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Контроллер для формирования отчетов
 */
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "API для формирования отчетов")
public class ReportController {
    
    private final ReportService reportService;
    
    @GetMapping("/requests")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MOL')")
    @Operation(summary = "Отчет по заявкам за период")
    public ResponseEntity<Map<String, Object>> getRequestsReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(reportService.generateRequestsReport(startDate, endDate, departmentId));
    }
    
    @GetMapping("/material-consumption")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MOL')")
    @Operation(summary = "Отчет по расходу материалов за период")
    public ResponseEntity<Map<String, Object>> getMaterialConsumptionReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(reportService.generateMaterialConsumptionReport(startDate, endDate, departmentId));
    }
    
    @GetMapping("/requests/export/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MOL')")
    @Operation(summary = "Экспорт отчета по заявкам в Excel")
    public ResponseEntity<byte[]> exportRequestsReportToExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Long departmentId) throws IOException {
        
        byte[] data = reportService.exportRequestsReportToExcel(startDate, endDate, departmentId);
        
        String filename = "Отчет_по_заявкам_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }
    
    @GetMapping("/material-consumption/export/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MOL')")
    @Operation(summary = "Экспорт отчета по расходу материалов в Excel")
    public ResponseEntity<byte[]> exportMaterialConsumptionReportToExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Long departmentId) throws IOException {
        
        byte[] data = reportService.exportMaterialConsumptionReportToExcel(startDate, endDate, departmentId);
        
        String filename = "Отчет_по_расходу_материалов_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }
}
