package com.systemtmc.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для подразделения
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDTO {
    private Long id;
    
    @NotBlank(message = "Код подразделения обязателен")
    @Size(max = 20)
    private String code;
    
    @NotBlank(message = "Название подразделения обязательно")
    @Size(max = 200)
    private String name;
    
    private String description;
    private Long parentId;
    private String parentName;
    private Boolean active;
    private LocalDateTime createdAt;
}
