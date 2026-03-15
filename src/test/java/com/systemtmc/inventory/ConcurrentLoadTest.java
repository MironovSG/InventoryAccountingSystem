package com.systemtmc.inventory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Нагрузочный тест: имитация одновременной работы 50 пользователей без потери производительности.
 * Проверяет требования:
 * 1) одновременный доступ пользователей с разными правами к своим функциям без конфликтов;
 * 2) стабильная работа при одновременных попытках авторизации;
 * 3) доступность ключевых функций и их стабильная работа.
 * Результаты наглядно выводятся в консоль после каждого теста.
 */
@DisplayName("Нагрузочный тест — 50 пользователей, одновременность и стабильность")
public class ConcurrentLoadTest extends BaseWebTest {

    private static final int CONCURRENT_USERS = 50;
    private static final String SEP = "══════════════════════════════════════════════════════════════";
    private static final String THIN = "──────────────────────────────────────────────────────────────";

    static {
        System.out.println();
        System.out.println(SEP);
        System.out.println("  НАГРУЗОЧНОЕ ТЕСТИРОВАНИЕ — 50 одновременных пользователей");
        System.out.println("  Требования: 1.4.2.3.2, 1.4.2.3.3, 1.4.2.3.4");
        System.out.println(SEP);
    }

    /** Выводит в консоль читаемый блок отчёта по тесту. */
    private static void printReport(String title, String requirementId, String[] lines, String verdict) {
        System.out.println();
        System.out.println(SEP);
        System.out.println("  " + title);
        System.out.println("  Требование: " + requirementId);
        System.out.println(THIN);
        for (String line : lines) {
            System.out.println("  " + line);
        }
        System.out.println(THIN);
        System.out.println("  Итог: " + verdict);
        System.out.println(SEP);
        System.out.println();
    }
    private static final int MAX_RESPONSE_TIME_MS = 10_000;
    private static final String BASE_PASSWORD = "test123";

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String baseUrl() {
        return "http://localhost:" + port + "/api";
    }

    /** Выполняет вход и возвращает JWT или null при ошибке. */
    private String login(String username, String password) {
        String url = baseUrl() + "/auth/login";
        String body = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    String.class
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode node = objectMapper.readTree(response.getBody());
                return node.has("token") ? node.get("token").asText() : null;
            }
        } catch (Exception e) {
            // ошибка сети или парсинга — возвращаем null
        }
        return null;
    }

    /** GET с заголовком Authorization. Возвращает код ответа или -1 при ошибке. */
    private int getWithToken(String path, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl() + path,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );
            return response.getStatusCode().value();
        } catch (Exception ignored) {
            return -1;
        }
    }

    @Test
    @DisplayName("1.4.2.3.2 + 1.4.2.3.3: Стабильная работа при одновременной авторизации 50 пользователей")
    void concurrentLoginsWithoutConflicts() throws Exception {
        // Смесь пользователей с разными ролями — все одновременно логинятся
        String[][] users = {
                {"admin", "admin123"},
                {"manager", BASE_PASSWORD},
                {"mol_user", BASE_PASSWORD},
                {"engineer", BASE_PASSWORD},
                {"specialist_arm", BASE_PASSWORD}
        };
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_USERS);
        List<Future<LoginResult>> futures = new ArrayList<>();
        long start = System.currentTimeMillis();

        for (int i = 0; i < CONCURRENT_USERS; i++) {
            String[] creds = users[i % users.length];
            futures.add(executor.submit(() -> {
                long t0 = System.currentTimeMillis();
                String token = login(creds[0], creds[1]);
                long elapsed = System.currentTimeMillis() - t0;
                return new LoginResult(token != null, elapsed);
            }));
        }

        List<LoginResult> results = new ArrayList<>();
        for (Future<LoginResult> f : futures) {
            results.add(f.get(15, TimeUnit.SECONDS));
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        long totalTime = System.currentTimeMillis() - start;

        long successCount = results.stream().filter(r -> r.success).count();
        long failedCount = results.stream().filter(r -> !r.success).count();
        OptionalLong maxResponseTime = results.stream().mapToLong(r -> r.responseTimeMs).max();

        assertThat(successCount)
                .as("Все 50 попыток авторизации должны завершиться успешно без конфликтов")
                .isEqualTo(CONCURRENT_USERS);
        assertThat(failedCount).isZero();
        assertThat(maxResponseTime.orElse(0))
                .as("Время отклика авторизации не должно превышать %d мс", MAX_RESPONSE_TIME_MS)
                .isLessThanOrEqualTo(MAX_RESPONSE_TIME_MS);

        long minTime = results.stream().mapToLong(r -> r.responseTimeMs).min().orElse(0);
        double avgTime = results.stream().mapToLong(r -> r.responseTimeMs).average().orElse(0);
        String verdict = failedCount == 0 && maxResponseTime.orElse(0) <= MAX_RESPONSE_TIME_MS
                ? "ПРОЙДЕН — все 50 авторизаций успешны, производительность в норме."
                : "ПРОВАЛЕН — есть ошибки или превышено время отклика.";
        printReport(
                "Одновременная авторизация 50 пользователей",
                "1.4.2.3.3 — стабильная работа при одновременных попытках авторизации",
                new String[]{
                        "Одновременных пользователей (потоков) : " + CONCURRENT_USERS,
                        "Успешных авторизаций                  : " + successCount,
                        "Неуспешных (ошибки/конфликты)          : " + failedCount,
                        "Общее время теста                      : " + totalTime + " мс",
                        "Время отклика — мин / среднее / макс   : " + minTime + " / " + (long) avgTime + " / " + maxResponseTime.orElse(0) + " мс",
                        "Допустимый максимум времени отклика    : " + MAX_RESPONSE_TIME_MS + " мс"
                },
                verdict
        );
    }

    @Test
    @DisplayName("1.4.2.3.2: Одновременный доступ пользователей с разными правами к своим функциям без конфликтов")
    void concurrentAccessDifferentRolesWithoutConflicts() throws Exception {
        String[][] users = {
                {"admin", "admin123"},
                {"manager", BASE_PASSWORD},
                {"mol_user", BASE_PASSWORD},
                {"engineer", BASE_PASSWORD},
                {"specialist_arm", BASE_PASSWORD}
        };
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_USERS);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictOrErrorCount = new AtomicInteger(0);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < CONCURRENT_USERS; i++) {
            String[] creds = users[i % users.length];
            futures.add(executor.submit(() -> {
                String token = login(creds[0], creds[1]);
                if (token == null) {
                    conflictOrErrorCount.incrementAndGet();
                    return;
                }
                // Каждый пользователь вызывает ключевые функции, доступные по роли
                int me = getWithToken("/auth/me", token);
                int materials = getWithToken("/materials", token);
                int requests = getWithToken("/requests", token);
                int departments = getWithToken("/departments", token);
                if (me == 200 && (materials == 200 || materials == 403) && (requests == 200 || requests == 403) && departments == 200) {
                    successCount.incrementAndGet();
                } else {
                    conflictOrErrorCount.incrementAndGet();
                }
            }));
        }

        for (Future<?> f : futures) {
            f.get(20, TimeUnit.SECONDS);
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        assertThat(successCount.get())
                .as("Все 50 пользователей с разными правами должны получить доступ к своим функциям без конфликтов")
                .isEqualTo(CONCURRENT_USERS);
        assertThat(conflictOrErrorCount.get()).isZero();

        String verdict = conflictOrErrorCount.get() == 0
                ? "ПРОЙДЕН — все 50 пользователей (5 ролей) работают без конфликтов."
                : "ПРОВАЛЕН — зафиксированы конфликты или ошибки доступа.";
        printReport(
                "Одновременный доступ с разными правами",
                "1.4.2.3.2 — одновременный доступ с различными правами без конфликтов",
                new String[]{
                        "Одновременных пользователей (потоков) : " + CONCURRENT_USERS,
                        "Роли в тесте                         : ADMIN, MANAGER, MOL, ENGINEER, SPECIALIST_ARM",
                        "Пользователей с успешным доступом    : " + successCount.get() + " / " + CONCURRENT_USERS,
                        "Конфликтов / ошибок доступа          : " + conflictOrErrorCount.get(),
                        "Проверяемые эндпоинты                : /auth/me, /materials, /requests, /departments"
                },
                verdict
        );
    }

    @Test
    @DisplayName("1.4.2.3.4: Доступность ключевых функций и стабильная работа под нагрузкой 50 пользователей")
    void keyFunctionsAvailabilityAndStabilityUnderLoad() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_USERS);
        AtomicInteger totalCalls = new AtomicInteger(0);
        AtomicInteger successCalls = new AtomicInteger(0);
        List<Long> responseTimes = Collections.synchronizedList(new ArrayList<>());
        String username = "admin";
        String password = "admin123";

        String[] keyPaths = {"/auth/me", "/materials", "/requests", "/departments", "/notifications/unread"};

        for (int i = 0; i < CONCURRENT_USERS; i++) {
            executor.submit(() -> {
                String token = login(username, password);
                if (token == null) return;
                for (String path : keyPaths) {
                    totalCalls.incrementAndGet();
                    long t0 = System.currentTimeMillis();
                    int code = getWithToken(path, token);
                    long elapsed = System.currentTimeMillis() - t0;
                    responseTimes.add(elapsed);
                    if (code == 200) successCalls.incrementAndGet();
                }
            });
        }

        executor.shutdown();
        assertThat(executor.awaitTermination(60, TimeUnit.SECONDS)).isTrue();

        int total = totalCalls.get();
        int success = successCalls.get();
        long maxTime = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        double avgTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);

        assertThat(success)
                .as("Ключевые функции должны быть доступны и стабильны: успешных вызовов должно быть не меньше 95%%")
                .isGreaterThanOrEqualTo((int) (total * 0.95));
        assertThat(maxTime)
                .as("Максимальное время отклика ключевой функции не должно превышать %d мс", MAX_RESPONSE_TIME_MS)
                .isLessThanOrEqualTo(MAX_RESPONSE_TIME_MS);

        int failCount = total - success;
        double successRate = total > 0 ? 100.0 * success / total : 0;
        long minTime = responseTimes.stream().mapToLong(Long::longValue).min().orElse(0);
        String verdict = successRate >= 95 && maxTime <= MAX_RESPONSE_TIME_MS
                ? "ПРОЙДЕН — ключевые функции доступны и стабильны под нагрузкой."
                : "ПРОВАЛЕН — доля успешных вызовов < 95% или превышено время отклика.";
        printReport(
                "Доступность ключевых функций под нагрузкой",
                "1.4.2.3.4 — доступность ключевых функций и стабильная работа",
                new String[]{
                        "Одновременных пользователей (потоков) : " + CONCURRENT_USERS,
                        "Всего вызовов ключевых API           : " + total,
                        "Успешных (HTTP 200)                  : " + success,
                        "Неуспешных                           : " + failCount,
                        "Доля успешных                         : " + String.format("%.1f", successRate) + " %",
                        "Время отклика — мин / среднее / макс  : " + minTime + " / " + (long) avgTime + " / " + maxTime + " мс",
                        "Ключевые эндпоинты                    : /auth/me, /materials, /requests, /departments, /notifications/unread"
                },
                verdict
        );
    }

    private static class LoginResult {
        final boolean success;
        final long responseTimeMs;

        LoginResult(boolean success, long responseTimeMs) {
            this.success = success;
            this.responseTimeMs = responseTimeMs;
        }
    }
}
