package com.systemtmc.inventory.controller;

import com.systemtmc.inventory.dto.RequestDTO;
import com.systemtmc.inventory.model.enums.RequestStatus;
import com.systemtmc.inventory.security.UserPrincipal;
import com.systemtmc.inventory.service.RequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Контроллер для управления заявками
 */
@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Tag(name = "Requests", description = "API для управления заявками на выдачу ТМЦ")
public class RequestController {
    
    private final RequestService requestService;
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MOL')")
    @Operation(summary = "Получить все заявки")
    public ResponseEntity<List<RequestDTO>> getAllRequests() {
        return ResponseEntity.ok(requestService.getAllRequests());
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MOL', 'ENGINEER')")
    @Operation(summary = "Получить заявку по ID")
    public ResponseEntity<RequestDTO> getRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.getRequestById(id));
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MOL')")
    @Operation(summary = "Получить заявки по статусу")
    public ResponseEntity<List<RequestDTO>> getRequestsByStatus(@PathVariable RequestStatus status) {
        return ResponseEntity.ok(requestService.getRequestsByStatus(status));
    }
    
    @GetMapping("/my-requests")
    @PreAuthorize("hasAnyRole('ENGINEER', 'MANAGER', 'MOL')")
    @Operation(summary = "Получить мои заявки")
    public ResponseEntity<List<RequestDTO>> getMyRequests(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(requestService.getRequestsByUser(userPrincipal.getId()));
    }
    
    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MOL')")
    @Operation(summary = "Получить заявки по подразделению")
    public ResponseEntity<List<RequestDTO>> getRequestsByDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(requestService.getRequestsByDepartment(departmentId));
    }
    
    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MOL')")
    @Operation(summary = "Получить заявки за период")
    public ResponseEntity<List<RequestDTO>> getRequestsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(requestService.getRequestsByDateRange(startDate, endDate));
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ENGINEER', 'MANAGER')")
    @Operation(summary = "Создать заявку")
    public ResponseEntity<RequestDTO> createRequest(
            @Valid @RequestBody RequestDTO requestDTO,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(requestService.createRequest(requestDTO, userPrincipal.getId()));
    }
    
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MOL', 'MANAGER')")
    @Operation(summary = "Обновить статус заявки")
    public ResponseEntity<RequestDTO> updateRequestStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        RequestStatus newStatus = RequestStatus.valueOf((String) updates.get("status"));
        String reason = updates.get("reason") != null ? (String) updates.get("reason") : null;
        
        return ResponseEntity.ok(requestService.updateRequestStatus(id, newStatus, userPrincipal.getId(), reason));
    }
    
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MOL', 'MANAGER')")
    @Operation(summary = "Одобрить заявку")
    public ResponseEntity<RequestDTO> approveRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(
            requestService.updateRequestStatus(id, RequestStatus.AWAITING_ISSUANCE, userPrincipal.getId(), null)
        );
    }
    
    @PostMapping("/{id}/issue")
    @PreAuthorize("hasAnyRole('ADMIN', 'MOL')")
    @Operation(summary = "Выдать материалы по заявке")
    public ResponseEntity<RequestDTO> issueRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(
            requestService.updateRequestStatus(id, RequestStatus.ISSUED, userPrincipal.getId(), null)
        );
    }
    
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'MOL', 'MANAGER')")
    @Operation(summary = "Отклонить заявку")
    public ResponseEntity<RequestDTO> rejectRequest(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        String reason = body.get("reason");
        return ResponseEntity.ok(
            requestService.updateRequestStatus(id, RequestStatus.REJECTED, userPrincipal.getId(), reason)
        );
    }
}
