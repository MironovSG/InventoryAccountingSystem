package com.systemtmc.inventory.service;

import com.systemtmc.inventory.dto.RequestDTO;
import com.systemtmc.inventory.dto.RequestItemDTO;
import com.systemtmc.inventory.model.entity.*;
import com.systemtmc.inventory.model.enums.MovementType;
import com.systemtmc.inventory.model.enums.RequestStatus;
import com.systemtmc.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для работы с заявками
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RequestService {
    
    private final RequestRepository requestRepository;
    private final RequestItemRepository requestItemRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final MaterialRepository materialRepository;
    private final MaterialMovementService movementService;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final PushNotificationService pushNotificationService;
    
    @Transactional(readOnly = true)
    public List<RequestDTO> getAllRequests() {
        return requestRepository.findAllRequests().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public RequestDTO getRequestById(Long id) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Заявка не найдена с ID: " + id));
        return convertToDTO(request);
    }
    
    @Transactional(readOnly = true)
    public List<RequestDTO> getRequestsByStatus(RequestStatus status) {
        return requestRepository.findRequestsByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<RequestDTO> getRequestsByUser(Long userId) {
        return requestRepository.findRequestsByUser(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<RequestDTO> getRequestsByDepartment(Long departmentId) {
        return requestRepository.findRequestsByDepartment(departmentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<RequestDTO> getRequestsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return requestRepository.findRequestsByDateRange(startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public RequestDTO createRequest(RequestDTO requestDTO, Long requesterId) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        Department department = requester.getDepartment();
        if (department == null && requestDTO.getDepartmentId() != null) {
            department = departmentRepository.findById(requestDTO.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Подразделение не найдено"));
        }
        
        if (department == null) {
            throw new RuntimeException("Подразделение не указано");
        }
        
        Request request = Request.builder()
                .requestNumber(generateRequestNumber())
                .status(RequestStatus.ACCEPTED)
                .requester(requester)
                .department(department)
                .purpose(requestDTO.getPurpose())
                .notes(requestDTO.getNotes())
                .priority(requestDTO.getPriority() != null ? requestDTO.getPriority() : 0)
                .expectedDate(requestDTO.getExpectedDate())
                .build();
        
        // Добавление позиций
        for (RequestItemDTO itemDTO : requestDTO.getItems()) {
            Material material = materialRepository.findById(itemDTO.getMaterialId())
                    .orElseThrow(() -> new RuntimeException("Материал не найден"));
            
            RequestItem item = RequestItem.builder()
                    .material(material)
                    .requestedQuantity(itemDTO.getRequestedQuantity())
                    .notes(itemDTO.getNotes())
                    .build();
            
            request.addItem(item);
        }
        
        Request savedRequest = requestRepository.save(request);
        
        auditService.logAction("CREATE_REQUEST", "Request", savedRequest.getId(), 
                "Создана заявка: " + savedRequest.getRequestNumber());
        
        // Отправка уведомлений МОЛ о новой заявке
        notificationService.sendRequestCreatedNotification(savedRequest);
        
        // Отправка push-уведомления МОЛ о новой заявке
        pushNotificationService.sendNewRequestNotificationToMOL(savedRequest);
        
        log.info("Создана заявка: {}", savedRequest.getRequestNumber());
        return convertToDTO(savedRequest);
    }
    
    @Transactional
    public RequestDTO updateRequestStatus(Long id, RequestStatus newStatus, Long userId, String reason) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Заявка не найдена"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        RequestStatus oldStatus = request.getStatus();
        request.setStatus(newStatus);
        
        switch (newStatus) {
            case IN_PROGRESS:
                break;
            case AWAITING_ISSUANCE:
                request.setApprovedBy(user);
                request.setApprovedAt(LocalDateTime.now());
                break;
            case ISSUED:
                request.setIssuedBy(user);
                request.setIssuedAt(LocalDateTime.now());
                // Создание движений материалов
                issueRequestItems(request);
                break;
            case CLOSED:
                break;
            case REJECTED:
                request.setRejectionReason(reason);
                break;
        }
        
        Request updatedRequest = requestRepository.save(request);
        
        auditService.logAction("UPDATE_REQUEST_STATUS", "Request", updatedRequest.getId(), 
                "Изменен статус заявки: " + oldStatus + " -> " + newStatus);
        
        // Отправка уведомлений о изменении статуса
        notificationService.sendRequestStatusChangedNotification(updatedRequest, oldStatus);
        
        // Отправка push-уведомления пользователю
        if (newStatus == RequestStatus.IN_PROGRESS) {
            pushNotificationService.sendRequestAcceptedNotification(updatedRequest);
        } else if (newStatus == RequestStatus.ISSUED) {
            pushNotificationService.sendRequestIssuedNotification(updatedRequest);
        } else if (newStatus == RequestStatus.REJECTED) {
            pushNotificationService.sendRequestRejectedNotification(updatedRequest);
        }
        
        log.info("Изменен статус заявки {} с {} на {}", 
                updatedRequest.getRequestNumber(), oldStatus, newStatus);
        
        return convertToDTO(updatedRequest);
    }
    
    @Transactional
    public void issueRequestItems(Request request) {
        for (RequestItem item : request.getItems()) {
            Material material = item.getMaterial();
            BigDecimal issuedQty = item.getApprovedQuantity() != null ? 
                    item.getApprovedQuantity() : item.getRequestedQuantity();
            
            if (!material.hasEnough(issuedQty)) {
                throw new RuntimeException("Недостаточно материала: " + material.getName());
            }
            
            // Обновление остатка
            BigDecimal newQuantity = material.getCurrentQuantity().subtract(issuedQty);
            material.setCurrentQuantity(newQuantity);
            materialRepository.save(material);
            
            // Создание движения
            movementService.createMovement(
                    material.getId(),
                    MovementType.ISSUANCE,
                    issuedQty,
                    request.getId(),
                    request.getRequester().getId(),
                    request.getDepartment().getId(),
                    request.getRequestNumber(),
                    "Выдача по заявке: " + request.getRequestNumber()
            );
            
            item.setIssuedQuantity(issuedQty);
        }
    }
    
    private String generateRequestNumber() {
        String prefix = "REQ";
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = requestRepository.count() + 1;
        return String.format("%s-%s-%04d", prefix, datePart, count);
    }
    
    private RequestDTO convertToDTO(Request request) {
        RequestDTO dto = RequestDTO.builder()
                .id(request.getId())
                .requestNumber(request.getRequestNumber())
                .status(request.getStatus())
                .requesterId(request.getRequester().getId())
                .requesterName(request.getRequester().getFullName())
                .departmentId(request.getDepartment().getId())
                .departmentName(request.getDepartment().getName())
                .purpose(request.getPurpose())
                .notes(request.getNotes())
                .priority(request.getPriority())
                .expectedDate(request.getExpectedDate())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
        
        if (request.getApprovedBy() != null) {
            dto.setApprovedById(request.getApprovedBy().getId());
            dto.setApprovedByName(request.getApprovedBy().getFullName());
            dto.setApprovedAt(request.getApprovedAt());
        }
        
        if (request.getIssuedBy() != null) {
            dto.setIssuedById(request.getIssuedBy().getId());
            dto.setIssuedByName(request.getIssuedBy().getFullName());
            dto.setIssuedAt(request.getIssuedAt());
        }
        
        dto.setRejectionReason(request.getRejectionReason());
        
        // Конвертация позиций
        List<RequestItemDTO> items = request.getItems().stream()
                .map(this::convertItemToDTO)
                .collect(Collectors.toList());
        dto.setItems(items);
        
        return dto;
    }
    
    private RequestItemDTO convertItemToDTO(RequestItem item) {
        return RequestItemDTO.builder()
                .id(item.getId())
                .materialId(item.getMaterial().getId())
                .materialName(item.getMaterial().getName())
                .materialArticle(item.getMaterial().getArticle())
                .unitOfMeasure(item.getMaterial().getUnitOfMeasure())
                .requestedQuantity(item.getRequestedQuantity())
                .approvedQuantity(item.getApprovedQuantity())
                .issuedQuantity(item.getIssuedQuantity())
                .availableQuantity(item.getMaterial().getCurrentQuantity())
                .notes(item.getNotes())
                .build();
    }
}
