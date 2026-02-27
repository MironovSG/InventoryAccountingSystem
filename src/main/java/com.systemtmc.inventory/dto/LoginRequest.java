package com.systemtmc.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO для запроса аутентификации
 */
@Data
public class LoginRequest {
    
    @NotBlank(message = "Имя пользователя обязательно")
    private String username;
    
    @NotBlank(message = "Пароль обязателен")
    private String password;
}
