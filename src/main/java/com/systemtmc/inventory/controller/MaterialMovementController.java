package com.systemtmc.inventory.controller;

import com.systemtmc.inventory.dto.MaterialMovementDTO;
import com.systemtmc.inventory.model.enums.MovementType;
import com.systemtmc.inventory.service.MaterialMovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Контроллер для управления движениями ТМЦ
 */
@RestController
@RequestMapping("/movements")
@RequiredArgsConstructor
@Tag(name = "Material Movements", description = "API для управления движениями ТМЦ")
public class MaterialMovementController {
    
    private final MaterialMovementService movementService;
    
    @GetMapping("/material/{materialId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MOL')")
    @Operation(summary = "Получить движения по материалу")
    public ResponseEntity<List<MaterialMovementDTO>> getMovementsByMaterial(@PathVariable Long materialId) {
        return ResponseEntity.ok(movementService.getMovementsByMaterial(materialId));
    }
    
    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MOL')")
    @Operation(summary = "Получить движения за период")
    public ResponseEntity<List<MaterialMovementDTO>> getMovementsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(movementService.getMovementsByDateRange(startDate, endDate));
    }
    
    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MOL')")
    @Operation(summary = "Получить движения по подразделению за период")
    public ResponseEntity<List<MaterialMovementDTO>> getMovementsByDepartmentAndDateRange(
            @PathVariable Long departmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(
            movementService.getMovementsByDepartmentAndDateRange(departmentId, startDate, endDate)
        );
    }
    
    @PostMapping("/receipt")
    @PreAuthorize("hasAnyRole('ADMIN', 'MOL')")
    @Operation(summary = "Поступление материала")
    public ResponseEntity<MaterialMovementDTO> receiptMaterial(@RequestBody Map<String, Object> data) {
        Long materialId = Long.valueOf(data.get("materialId").toString());
        BigDecimal quantity = new BigDecimal(data.get("quantity").toString());
        String documentNumber = (String) data.get("documentNumber");
        String notes = (String) data.get("notes");
        
        return ResponseEntity.ok(
            movementService.receiptMaterial(materialId, quantity, documentNumber, notes)
        );
    }
    
    @PostMapping("/adjust")
    @PreAuthorize("hasAnyRole('ADMIN', 'MOL')")
    @Operation(summary = "Корректировка остатка материала")
    public ResponseEntity<MaterialMovementDTO> adjustMaterial(@RequestBody Map<String, Object> data) {
        Long materialId = Long.valueOf(data.get("materialId").toString());
        BigDecimal newQuantity = new BigDecimal(data.get("newQuantity").toString());
        String reason = (String) data.get("reason");
        
        return ResponseEntity.ok(
            movementService.adjustMaterial(materialId, newQuantity, reason)
        );
    }
    
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'MOL')")
    @Operation(summary = "Создать движение материала")
    public ResponseEntity<MaterialMovementDTO> createMovement(@RequestBody Map<String, Object> data) {
        Long materialId = Long.valueOf(data.get("materialId").toString());
        MovementType movementType = MovementType.valueOf((String) data.get("movementType"));
        BigDecimal quantity = new BigDecimal(data.get("quantity").toString());
        Long requestId = data.get("requestId") != null ? Long.valueOf(data.get("requestId").toString()) : null;
        Long userId = data.get("userId") != null ? Long.valueOf(data.get("userId").toString()) : null;
        Long departmentId = data.get("departmentId") != null ? Long.valueOf(data.get("departmentId").toString()) : null;
        String documentNumber = (String) data.get("documentNumber");
        String notes = (String) data.get("notes");
        
        return ResponseEntity.ok(
            movementService.createMovement(materialId, movementType, quantity, 
                requestId, userId, departmentId, documentNumber, notes)
        );
    }
}
