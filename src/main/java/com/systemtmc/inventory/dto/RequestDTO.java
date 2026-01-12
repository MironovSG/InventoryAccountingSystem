package com.systemtmc.inventory.dto;

import com.systemtmc.inventory.model.enums.RequestStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO для заявки
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestDTO {
    private Long id;
    private String requestNumber;
    private RequestStatus status;
    
    private Long requesterId;
    private String requesterName;
    
    private Long departmentId;
    private String departmentName;
    
    @NotBlank(message = "Назначение обязательно")
    @Size(max = 500)
    private String purpose;
    
    private String notes;
    
    @NotEmpty(message = "Список материалов не может быть пустым")
    @Builder.Default
    private List<RequestItemDTO> items = new ArrayList<>();
    
    private Long approvedById;
    private String approvedByName;
    private LocalDateTime approvedAt;
    
    private Long issuedById;
    private String issuedByName;
    private LocalDateTime issuedAt;
    
    private String rejectionReason;
    private Integer priority;
    private LocalDateTime expectedDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
