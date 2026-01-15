package com.systemtmc.inventory.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Утилита для генерации BCrypt хешей паролей
 * Используйте этот класс для генерации правильных хешей для тестовых пользователей
 */
public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        System.out.println("=== BCrypt хеши для тестовых паролей ===");
        System.out.println("admin123: " + encoder.encode("admin123"));
        System.out.println("mol123: " + encoder.encode("mol123"));
        System.out.println("engineer123: " + encoder.encode("engineer123"));
        System.out.println("manager123: " + encoder.encode("manager123"));
    }
}
