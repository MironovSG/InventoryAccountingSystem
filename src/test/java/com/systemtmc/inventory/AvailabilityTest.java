package com.systemtmc.inventory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Тесты: Доступность системы (Availability).
 * Критические эндпоинты должны быть доступны и возвращать ожидаемый статус.
 */
@DisplayName("Доступность системы")
public class AvailabilityTest extends BaseWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Эндпоинт входа доступен и принимает POST")
    void loginEndpointAvailable() throws Exception {
        mockMvc.perform(post(url("auth/login"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("admin"));
        System.out.println("[Availability] POST /api/auth/login — доступен, возвращает токен и username.");
    }

    @Test
    @DisplayName("Эндпоинт /auth/me доступен (требует аутентификации)")
    void authMeEndpointReachable() throws Exception {
        mockMvc.perform(get(url("auth/me")))
                .andExpect(status().is4xxClientError());
        System.out.println("[Availability] GET /api/auth/me — доступен (401/400 без токена).");
    }

    @Test
    @DisplayName("Защищённый эндпоинт /materials доступен и возвращает 401 без токена")
    void protectedEndpointRequiresAuth() throws Exception {
        mockMvc.perform(get(url("materials")))
                .andExpect(status().isUnauthorized());
        System.out.println("[Availability] GET /api/materials без токена — 401 (доступность проверена).");
    }
}
