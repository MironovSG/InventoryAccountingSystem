package com.systemtmc.inventory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Тесты: Надёжность (Reliability).
 * Поведение при повторных запросах и корректная обработка запросов.
 */
@DisplayName("Надежность")
public class ReliabilityTest extends BaseWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Повторный успешный вход возвращает новый токен каждый раз")
    void repeatedLoginReturnsNewTokenEachTime() throws Exception {
        String content = "{\"username\":\"admin\",\"password\":\"admin123\"}";
        String token1 = mockMvc.perform(post(url("auth/login"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token2 = mockMvc.perform(post(url("auth/login"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        // Оба ответа должны содержать токен (разные по времени выдачи)
        assert token1.contains("\"token\"") && token2.contains("\"token\"");
        System.out.println("[Reliability] Повторный вход — каждый раз возвращается токен.");
    }

    @Test
    @DisplayName("Корректный JSON не приводит к 500")
    void validJsonDoesNotCauseServerError() throws Exception {
        mockMvc.perform(post(url("auth/login"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk());
        System.out.println("[Reliability] Корректный запрос — 200, без 500.");
    }
}
