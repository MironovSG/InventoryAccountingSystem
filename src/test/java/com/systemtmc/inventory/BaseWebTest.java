package com.systemtmc.inventory;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Базовый класс для веб-тестов: поднимает контекст с профилем test и предоставляет MockMvc.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseWebTest {

    /** В тестах MockMvc обращается к сервлету без context-path, используем путь без /api */
    protected String url(String path) {
        if (path.startsWith("/")) return path;
        return "/" + path;
    }
}
