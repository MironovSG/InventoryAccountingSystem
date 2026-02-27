package com.systemtmc.inventory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Тесты: Адаптация интерфейса (API-контракт).
 * Проверка формата ответов (Content-Type, структура JSON).
 */
@DisplayName("Адаптация интерфейса (API)")
public class InterfaceAdaptationTest extends BaseWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Ответ логина — application/json")
    void loginResponseContentTypeIsJson() throws Exception {
        mockMvc.perform(post(url("auth/login"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        System.out.println("[Interface] Ответ /auth/login — Content-Type: application/json.");
    }

    @Test
    @DisplayName("Ответ логина содержит обязательные поля контракта")
    void loginResponseHasRequiredFields() throws Exception {
        mockMvc.perform(post(url("auth/login"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.username").exists())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.fullName").exists())
                .andExpect(jsonPath("$.role").exists());
        System.out.println("[Interface] Контракт ответа: token, userId, username, email, fullName, role.");
    }
}
