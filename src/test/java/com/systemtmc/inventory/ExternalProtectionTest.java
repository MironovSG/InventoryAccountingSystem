package com.systemtmc.inventory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тесты: Защита от внешних воздействий.
 * Валидация ввода, устойчивость к некорректным и потенциально опасным данным.
 */
@DisplayName("Защита от внешних воздействий")
public class ExternalProtectionTest extends BaseWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Пустое тело запроса не вызывает 500")
    void emptyBodyDoesNotCause500() throws Exception {
        mockMvc.perform(post(url("auth/login"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
        System.out.println("[ExternalProtection] Пустой JSON — 400, не 500.");
    }

    @Test
    @DisplayName("Некорректный JSON не вызывает 500")
    void invalidJsonDoesNotCause500() throws Exception {
        mockMvc.perform(post(url("auth/login"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid}"))
                .andExpect(status().is4xxClientError());
        System.out.println("[ExternalProtection] Некорректный JSON — 4xx, не 500.");
    }

    @Test
    @DisplayName("Очень длинный username не ломает сервер")
    void veryLongUsernameHandled() throws Exception {
        String longName = "a".repeat(10_000);
        mockMvc.perform(post(url("auth/login"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + longName + "\",\"password\":\"x\"}"))
                .andExpect(status().is4xxClientError());
        System.out.println("[ExternalProtection] Очень длинный ввод — обработан без 500.");
    }
}
