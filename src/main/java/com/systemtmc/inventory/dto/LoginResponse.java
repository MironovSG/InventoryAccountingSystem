package com.systemtmc.inventory.dto;

import com.systemtmc.inventory.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для ответа при аутентификации
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    @Builder.Default
    private String type = "Bearer";
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private UserRole role;
    private Long departmentId;
    private String departmentName;
}
