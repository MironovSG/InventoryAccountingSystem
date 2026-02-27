package com.systemtmc.inventory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Тесты: Безопасность (Security).
 * Проверка аутентификации, авторизации, кодирования паролей и валидации JWT.
 */
@DisplayName("Безопасность")
public class SecurityTest extends BaseWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Защищённые эндпоинты возвращают 401 без токена")
    void protectedEndpointsReturn401WithoutToken() throws Exception {
        mockMvc.perform(get(url("materials"))).andExpect(status().isUnauthorized());
        mockMvc.perform(get(url("users"))).andExpect(status().isUnauthorized());
        mockMvc.perform(get(url("requests"))).andExpect(status().isUnauthorized());
        System.out.println("[Security] Защищённые эндпоинты возвращают 401 без токена.");
    }

    @Test
    @DisplayName("Неверные учётные данные возвращают 400")
    void invalidCredentialsReturn400() throws Exception {
        mockMvc.perform(post(url("auth/login"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
                .andExpect(status().isBadRequest());
        System.out.println("[Security] Неверный пароль — 400.");
    }

    @Test
    @DisplayName("Валидация входа: пустой username отклоняется")
    void loginValidationRejectsBlankUsername() throws Exception {
        mockMvc.perform(post(url("auth/login"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"admin123\"}"))
                .andExpect(status().isBadRequest());
        System.out.println("[Security] Пустой username — 400.");
    }

    @Test
    @DisplayName("Успешный вход возвращает JWT и данные пользователя")
    void successfulLoginReturnsJwtAndUserData() throws Exception {
        mockMvc.perform(post(url("auth/login"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.role").value("ADMIN"));
        System.out.println("[Security] Успешный вход — JWT и роль возвращаются.");
    }

    @Test
    @DisplayName("Пароль кодируется (BCrypt)")
    void passwordIsEncoded() {
        String raw = "admin123";
        String encoded = passwordEncoder.encode(raw);
        assertThat(encoded).isNotEqualTo(raw);
        assertThat(encoded).startsWith("$2a$");
        assertThat(passwordEncoder.matches(raw, encoded)).isTrue();
        System.out.println("[Security] Пароль кодируется через BCrypt.");
    }
}
