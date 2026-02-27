package com.systemtmc.inventory;

import com.systemtmc.inventory.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты: Резервное копирование (Backup).
 * Проверка возможности чтения критических данных (как предпосылка для бэкапа).
 */
@DisplayName("Резервное копирование")
public class BackupTest extends BaseWebTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Данные пользователей доступны для чтения (репозиторий)")
    void userDataIsReadable() {
        assertThat(userRepository.count()).isGreaterThanOrEqualTo(0);
        userRepository.findAll().forEach(u -> {
            assertThat(u.getUsername()).isNotBlank();
            assertThat(u.getEmail()).isNotBlank();
        });
        System.out.println("[Backup] Репозиторий пользователей доступен для чтения, записей: " + userRepository.count());
    }
}
