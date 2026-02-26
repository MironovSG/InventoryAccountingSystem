package com.systemtmc.inventory.quality;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.systemtmc.inventory.dto.RequestDTO;
import com.systemtmc.inventory.dto.RequestItemDTO;
import com.systemtmc.inventory.model.enums.RequestStatus;
import com.systemtmc.inventory.model.enums.UserRole;
import com.systemtmc.inventory.security.JwtTokenProvider;
import com.systemtmc.inventory.security.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NonFunctionalRequirementsTest {

    private static final String JWT_SECRET = "myVerySecureTestSecretKeyForJwtProvider123456789";
    private static final long JWT_EXPIRATION_MS = 3_600_000L;

    @Test
    @DisplayName("Время отклика: генерация и валидация JWT выполняются быстро")
    void responseTimeTest() {
        JwtTokenProvider provider = buildTokenProvider();
        Authentication authentication = buildAuthentication();

        long start = System.nanoTime();
        String token = provider.generateToken(authentication);
        boolean valid = provider.validateToken(token);
        long durationMs = (System.nanoTime() - start) / 1_000_000;

        System.out.println("[ResponseTime] token flow duration(ms): " + durationMs);
        assertTrue(valid, "Сгенерированный токен должен быть валидным");
        assertTrue(durationMs < 250, "Ожидалось время отклика менее 250мс, факт: " + durationMs + "мс");
    }

    @Test
    @DisplayName("Доступность системы: JWT-провайдер стабильно отвечает на повторяющиеся запросы")
    void availabilityTest() {
        JwtTokenProvider provider = buildTokenProvider();
        Authentication authentication = buildAuthentication();

        long start = System.nanoTime();
        for (int i = 0; i < 200; i++) {
            String token = provider.generateToken(authentication);
            assertTrue(provider.validateToken(token), "Токен должен быть валидным на итерации " + i);
        }
        long durationMs = (System.nanoTime() - start) / 1_000_000;

        System.out.println("[Availability] 200 iterations duration(ms): " + durationMs);
        assertTrue(durationMs < 2_000, "Обработка повторяющихся запросов заняла слишком много времени: " + durationMs + "мс");
    }

    @Test
    @DisplayName("Надежность: извлеченный ID пользователя из JWT остается корректным")
    void reliabilityTest() {
        JwtTokenProvider provider = buildTokenProvider();
        Authentication authentication = buildAuthentication();

        String token = provider.generateToken(authentication);
        Long userId = provider.getUserIdFromToken(token);

        System.out.println("[Reliability] extracted userId: " + userId);
        assertEquals(101L, userId);
        assertTrue(provider.validateToken(token));
        assertTrue(provider.validateToken(token));
    }

    @Test
    @DisplayName("Безопасность: модифицированный токен отвергается")
    void securityTest() {
        JwtTokenProvider provider = buildTokenProvider();
        Authentication authentication = buildAuthentication();
        String token = provider.generateToken(authentication);

        String tamperedToken = token.substring(0, token.length() - 2) + "ab";
        boolean valid = provider.validateToken(tamperedToken);

        System.out.println("[Security] tampered token valid: " + valid);
        assertFalse(valid, "Подмененный токен не должен проходить валидацию");
    }

    @Test
    @DisplayName("Адаптация интерфейса: присутствуют viewport и media query для адаптивности")
    void interfaceAdaptationTest() throws Exception {
        String indexHtml = Files.readString(Path.of(new ClassPathResource("static/index.html").getURI()));
        String css = Files.readString(Path.of(new ClassPathResource("static/css/style.css").getURI()));

        boolean hasViewport = indexHtml.contains("name=\"viewport\"");
        boolean hasMediaQuery = css.contains("@media (max-width: 768px)");

        System.out.println("[UI Adaptation] viewport=" + hasViewport + ", mediaQuery=" + hasMediaQuery);
        assertTrue(hasViewport, "В index.html должен быть meta viewport");
        assertTrue(hasMediaQuery, "В style.css ожидается media query для мобильных устройств");
    }

    @Test
    @DisplayName("Резервное копирование: DTO корректно сохраняется и восстанавливается из JSON")
    void backupTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

        RequestDTO original = RequestDTO.builder()
                .id(77L)
                .requestNumber("REQ-2024-001")
                .status(RequestStatus.PENDING)
                .purpose("Проверка резервного копирования")
                .createdAt(LocalDateTime.now())
                .items(List.of(RequestItemDTO.builder().materialId(55L).quantityRequested(3).build()))
                .build();

        Path backupFile = Files.createTempFile("request-backup-", ".json");
        mapper.writeValue(backupFile.toFile(), original);
        RequestDTO restored = mapper.readValue(backupFile.toFile(), RequestDTO.class);

        System.out.println("[Backup] backup file: " + backupFile + ", restoredRequest=" + restored.getRequestNumber());
        assertEquals(original.getRequestNumber(), restored.getRequestNumber());
        assertEquals(original.getPurpose(), restored.getPurpose());
        assertEquals(original.getItems().size(), restored.getItems().size());
    }

    @Test
    @DisplayName("Защита от внешних воздействий: вредоносные входы не вызывают аварий и отвергаются")
    void externalInfluenceProtectionTest() {
        JwtTokenProvider provider = buildTokenProvider();

        List<String> malformedInputs = List.of(
                "' OR 1=1 --",
                "<script>alert('xss')</script>",
                "..\\..\\etc\\passwd",
                "this-is-not-a-jwt"
        );

        for (String candidate : malformedInputs) {
            boolean valid = provider.validateToken(candidate);
            System.out.println("[ExternalProtection] input='" + candidate + "' valid=" + valid);
            assertFalse(valid, "Некорректный вход должен быть отклонен: " + candidate);
        }
    }

    private JwtTokenProvider buildTokenProvider() {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", JWT_SECRET);
        ReflectionTestUtils.setField(provider, "jwtExpiration", JWT_EXPIRATION_MS);
        provider.init();
        return provider;
    }

    private Authentication buildAuthentication() {
        UserPrincipal principal = new UserPrincipal(
                101L,
                "test-user",
                "encoded-password",
                "test@example.com",
                UserRole.ADMIN,
                true,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }
}
