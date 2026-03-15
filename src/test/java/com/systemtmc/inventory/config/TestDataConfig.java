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

    private static final String TEST_PASSWORD = "test123";

    @Transactional
    @jakarta.annotation.PostConstruct
    public void init() {
        saveUserIfAbsent("admin", "admin123", "admin@test.local", "Admin", "Admin", UserRole.ADMIN);
        saveUserIfAbsent("manager", TEST_PASSWORD, "manager@test.local", "Manager", "Test", UserRole.MANAGER);
        saveUserIfAbsent("mol_user", TEST_PASSWORD, "mol@test.local", "Mol", "Test", UserRole.MOL);
        saveUserIfAbsent("engineer", TEST_PASSWORD, "engineer@test.local", "Engineer", "Test", UserRole.ENGINEER);
        saveUserIfAbsent("specialist_arm", TEST_PASSWORD, "arm@test.local", "Specialist", "ARM", UserRole.SPECIALIST_ARM);
    }

    private void saveUserIfAbsent(String username, String password, String email, String lastName, String firstName, UserRole role) {
        if (userRepository.findByUsername(username).isEmpty()) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setEmail(email);
            user.setLastName(lastName);
            user.setFirstName(firstName);
            user.setRole(role);
            user.setActive(true);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            user.setDeleted(false);
            userRepository.save(user);
        }
    }
}
