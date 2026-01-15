package com.systemtmc.inventory.service;

import com.systemtmc.inventory.dto.MaterialMovementDTO;
import com.systemtmc.inventory.model.entity.*;
import com.systemtmc.inventory.model.enums.MovementType;
import com.systemtmc.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Сервис для работы с движениями ТМЦ
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MaterialMovementService {
    
    private final MaterialMovementRepository movementRepository;
    private final MaterialRepository materialRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final RequestRepository requestRepository;
    private final AuditService auditService;
    
    @Transactional(readOnly = true)
    public List<MaterialMovementDTO> getMovementsByMaterial(Long materialId) {
        return movementRepository.findMovementsByMaterial(materialId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<MaterialMovementDTO> getMovementsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return movementRepository.findMovementsByDateRange(startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<MaterialMovementDTO> getMovementsByDepartmentAndDateRange(
            Long departmentId, LocalDateTime startDate, LocalDateTime endDate) {
        return movementRepository.findMovementsByDepartmentAndDateRange(departmentId, startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    @SuppressWarnings("null")
    public MaterialMovementDTO createMovement(
            Long materialId,
            MovementType movementType,
            BigDecimal quantity,
            Long requestId,
            Long userId,
            Long departmentId,
            String documentNumber,
            String notes) {
        
        Long nonNullMaterialId = Objects.requireNonNull(materialId, "ID материала не может быть null");
        Material material = materialRepository.findById(nonNullMaterialId)
                .orElseThrow(() -> new RuntimeException("Материал не найден"));
        
        BigDecimal quantityBefore = material.getCurrentQuantity();
        BigDecimal quantityAfter;
        
        // Вычисление нового остатка в зависимости от типа движения
        switch (movementType) {
            case RECEIPT:
            case RETURN:
                quantityAfter = quantityBefore.add(quantity);
                break;
            case ISSUANCE:
            case WRITE_OFF:
                quantityAfter = quantityBefore.subtract(quantity);
                if (quantityAfter.compareTo(BigDecimal.ZERO) < 0) {
                    throw new RuntimeException("Недостаточно материала на складе");
                }
                break;
            case ADJUSTMENT:
            case INVENTORY:
                quantityAfter = quantity;
                break;
            default:
                throw new RuntimeException("Неизвестный тип движения");
        }
        
        MaterialMovement movement = MaterialMovement.builder()
                .material(material)
                .movementType(movementType)
                .quantity(quantity)
                .quantityBefore(quantityBefore)
                .quantityAfter(quantityAfter)
                .documentNumber(documentNumber)
                .notes(notes)
                .build();
        
        if (requestId != null) {
            Request request = requestRepository.findById(requestId).orElse(null);
            movement.setRequest(request);
        }
        
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            movement.setUser(user);
        }
        
        if (departmentId != null) {
            Department department = departmentRepository.findById(departmentId).orElse(null);
            movement.setDepartment(department);
        }
        
        if (material.getUnitPrice() != null) {
            movement.setUnitPrice(material.getUnitPrice());
            movement.setTotalPrice(material.getUnitPrice().multiply(quantity));
        }
        
        // Обновление остатка материала
        material.setCurrentQuantity(quantityAfter);
        materialRepository.save(material);
        
        MaterialMovement savedMovement = Objects.requireNonNull(
                movementRepository.save(movement), 
                "Ошибка сохранения движения материала");
        Long movementId = Objects.requireNonNull(savedMovement.getId(), 
                "ID движения материала не может быть null");
        
        auditService.logAction("CREATE_MOVEMENT", "MaterialMovement", movementId, 
                String.format("%s материала %s: %s %s (остаток: %s -> %s)",
                        movementType.getDisplayName(),
                        material.getArticle(),
                        quantity,
                        material.getUnitOfMeasure(),
                        quantityBefore,
                        quantityAfter));
        
        log.info("Создано движение {} для материала {}: {} (остаток: {} -> {})",
                movementType, material.getArticle(), quantity, quantityBefore, quantityAfter);
        
        return convertToDTO(savedMovement);
    }
    
    @Transactional
    public MaterialMovementDTO receiptMaterial(Long materialId, BigDecimal quantity, 
                                              String documentNumber, String notes) {
        return createMovement(materialId, MovementType.RECEIPT, quantity, 
                null, null, null, documentNumber, notes);
    }
    
    @Transactional
    public MaterialMovementDTO adjustMaterial(Long materialId, BigDecimal newQuantity, String reason) {
        return createMovement(materialId, MovementType.ADJUSTMENT, newQuantity, 
                null, null, null, null, reason);
    }
    
    private MaterialMovementDTO convertToDTO(MaterialMovement movement) {
        MaterialMovementDTO dto = MaterialMovementDTO.builder()
                .id(movement.getId())
                .materialId(movement.getMaterial().getId())
                .materialName(movement.getMaterial().getName())
                .materialArticle(movement.getMaterial().getArticle())
                .movementType(movement.getMovementType())
                .quantity(movement.getQuantity())
                .quantityBefore(movement.getQuantityBefore())
                .quantityAfter(movement.getQuantityAfter())
                .documentNumber(movement.getDocumentNumber())
                .notes(movement.getNotes())
                .unitPrice(movement.getUnitPrice())
                .totalPrice(movement.getTotalPrice())
                .createdAt(movement.getCreatedAt())
                .build();
        
        if (movement.getRequest() != null) {
            dto.setRequestId(movement.getRequest().getId());
            dto.setRequestNumber(movement.getRequest().getRequestNumber());
        }
        
        if (movement.getUser() != null) {
            dto.setUserId(movement.getUser().getId());
            dto.setUserName(movement.getUser().getFullName());
        }
        
        if (movement.getDepartment() != null) {
            dto.setDepartmentId(movement.getDepartment().getId());
            dto.setDepartmentName(movement.getDepartment().getName());
        }
        
        return dto;
    }
}
