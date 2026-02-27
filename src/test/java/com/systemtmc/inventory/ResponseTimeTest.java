package com.systemtmc.inventory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тесты: Время отклика (Response Time).
 * Проверяем, что критические запросы выполняются быстрее заданного порога.
 */
@DisplayName("Время отклика")
public class ResponseTimeTest extends BaseWebTest {

    private static final long MAX_RESPONSE_TIME_MS = 3000;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Публичный эндпоинт /auth/login отвечает быстрее 3 сек")
    void loginEndpointResponseTime() throws Exception {
        long start = System.currentTimeMillis();
        mockMvc.perform(post(url("auth/login"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk());
        long elapsed = System.currentTimeMillis() - start;
        assertThat(elapsed).isLessThan(MAX_RESPONSE_TIME_MS);
        System.out.println("[ResponseTime] POST /auth/login: " + elapsed + " мс (порог: " + MAX_RESPONSE_TIME_MS + " мс)");
    }

    @Test
    @DisplayName("Запрос к публичному пути отвечает быстрее 3 сек")
    void publicResourceResponseTime() throws Exception {
        long start = System.currentTimeMillis();
        mockMvc.perform(get(url("auth/me"))).andExpect(status().is4xxClientError()); // 400 без токена — ожидаемо
        long elapsed = System.currentTimeMillis() - start;
        assertThat(elapsed).isLessThan(MAX_RESPONSE_TIME_MS);
        System.out.println("[ResponseTime] GET /auth/me (без токена): " + elapsed + " мс");
    }
}
