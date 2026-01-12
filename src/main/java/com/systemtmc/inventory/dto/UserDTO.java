package com.systemtmc.inventory.dto;

import com.systemtmc.inventory.model.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для пользователя
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    
    @NotBlank(message = "Имя пользователя обязательно")
    @Size(min = 3, max = 50)
    private String username;
    
    @NotBlank(message = "Email обязателен")
    @Email
    private String email;
    
    @NotBlank(message = "Фамилия обязательна")
    private String lastName;
    
    @NotBlank(message = "Имя обязательно")
    private String firstName;
    
    private String middleName;
    
    @NotNull(message = "Роль обязательна")
    private UserRole role;
    
    private Long departmentId;
    private String departmentName;
    
    private String phone;
    private String position;
    private Boolean active;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    
    // Поле для создания/обновления пароля (не возвращается в ответе)
    @Size(min = 6, message = "Пароль должен быть не менее 6 символов")
    private String password;
}
