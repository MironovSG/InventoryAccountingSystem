package com.systemtmc.inventory.config;

import com.systemtmc.inventory.model.entity.User;
import com.systemtmc.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * При первом запуске обновляет пароли тестовых пользователей, если в БД остался
 * старый placeholder-хеш (для "password"). Пароли из комментариев в V2__insert_initial_data.sql
 * станут рабочими: admin123, mol123, engineer123, manager123.
 */
@Component
@Order(100)
@RequiredArgsConstructor
@Slf4j
public class DefaultPasswordInitializer implements ApplicationRunner {

    /** Старый хеш из миграции (соответствует паролю "password") */
    private static final String LEGACY_PLACEHOLDER_HASH = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Map<String, String> DEFAULT_PASSWORDS = Map.of(
            "admin", "admin123",
            "mol_user", "mol123",
            "engineer_user", "engineer123",
            "manager_user", "manager123"
    );

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (Map.Entry<String, String> entry : DEFAULT_PASSWORDS.entrySet()) {
            String username = entry.getKey();
            String plainPassword = entry.getValue();
            userRepository.findByUsername(username).ifPresent(user -> {
                if (LEGACY_PLACEHOLDER_HASH.equals(user.getPassword())) {
                    user.setPassword(passwordEncoder.encode(plainPassword));
                    userRepository.save(user);
                    log.info("Обновлён пароль по умолчанию для пользователя: {}", username);
                }
            });
        }
    }
}
