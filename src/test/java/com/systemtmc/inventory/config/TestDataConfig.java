package com.systemtmc.inventory.config;

import com.systemtmc.inventory.model.entity.User;
import com.systemtmc.inventory.model.enums.UserRole;
import com.systemtmc.inventory.repository.UserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Заполняет БД тестовым пользователем для интеграционных тестов (профиль test).
 */
@Component
@Profile("test")
public class TestDataConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TestDataConfig(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @jakarta.annotation.PostConstruct
    public void init() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@test.local")
                    .lastName("Admin")
                    .firstName("Admin")
                    .role(UserRole.ADMIN)
                    .active(true)
                    .build();
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());
            admin.setDeleted(false);
            userRepository.save(admin);
        }
    }
}
