package com.systemtmc.inventory.model.entity;

import com.systemtmc.inventory.model.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Сущность пользователя системы
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_username", columnList = "username"),
    @Index(name = "idx_user_email", columnList = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {
    
    @NotBlank(message = "Имя пользователя обязательно")
    @Size(min = 3, max = 50, message = "Имя пользователя должно быть от 3 до 50 символов")
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;
    
    @NotBlank(message = "Пароль обязателен")
    @Column(name = "password", nullable = false)
    private String password;
    
    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный email")
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;
    
    @NotBlank(message = "Фамилия обязательна")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;
    
    @NotBlank(message = "Имя обязательно")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;
    
    @Column(name = "middle_name", length = 100)
    private String middleName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "position", length = 100)
    private String position;
    
    @Column(name = "work_group", length = 20)
    private String workGroup; // АРМ, ГОТО, ГОКС
    
    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * Получить полное имя пользователя
     */
    public String getFullName() {
        StringBuilder fullName = new StringBuilder(lastName).append(" ").append(firstName);
        if (middleName != null && !middleName.isEmpty()) {
            fullName.append(" ").append(middleName);
        }
        return fullName.toString();
    }
}
