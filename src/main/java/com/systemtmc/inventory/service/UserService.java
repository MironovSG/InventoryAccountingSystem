package com.systemtmc.inventory.service;

import com.systemtmc.inventory.dto.UserDTO;
import com.systemtmc.inventory.model.entity.Department;
import com.systemtmc.inventory.model.entity.User;
import com.systemtmc.inventory.model.enums.UserRole;
import com.systemtmc.inventory.repository.DepartmentRepository;
import com.systemtmc.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для работы с пользователями
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findByDeletedFalse().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<UserDTO> getActiveUsers() {
        return userRepository.findAllActiveUsers().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с ID: " + id));
        return convertToDTO(user);
    }
    
    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + username));
        return convertToDTO(user);
    }
    
    @Transactional(readOnly = true)
    public List<UserDTO> getUsersByRole(UserRole role) {
        return userRepository.findActiveUsersByRole(role).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<UserDTO> getUsersByDepartment(Long departmentId) {
        return userRepository.findByDepartmentId(departmentId).stream()
                .filter(user -> !user.getDeleted())
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<UserDTO> searchUsers(String search) {
        return userRepository.searchUsers(search).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }
        
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }
        
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setEmail(userDTO.getEmail());
        user.setLastName(userDTO.getLastName());
        user.setFirstName(userDTO.getFirstName());
        user.setMiddleName(userDTO.getMiddleName());
        user.setRole(userDTO.getRole());
        user.setPhone(userDTO.getPhone());
        user.setPosition(userDTO.getPosition());
        user.setActive(userDTO.getActive() != null ? userDTO.getActive() : true);
        
        if (userDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(userDTO.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Подразделение не найдено"));
            user.setDepartment(department);
        }
        
        User savedUser = userRepository.save(user);
        auditService.logAction("CREATE_USER", "User", savedUser.getId(), 
                "Создан пользователь: " + savedUser.getUsername());
        
        log.info("Создан пользователь: {}", savedUser.getUsername());
        return convertToDTO(savedUser);
    }
    
    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        // Проверка уникальности при изменении
        if (!user.getUsername().equals(userDTO.getUsername()) && 
            userRepository.existsByUsername(userDTO.getUsername())) {
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }
        
        if (!user.getEmail().equals(userDTO.getEmail()) && 
            userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }
        
        user.setEmail(userDTO.getEmail());
        user.setLastName(userDTO.getLastName());
        user.setFirstName(userDTO.getFirstName());
        user.setMiddleName(userDTO.getMiddleName());
        user.setRole(userDTO.getRole());
        user.setPhone(userDTO.getPhone());
        user.setPosition(userDTO.getPosition());
        user.setActive(userDTO.getActive());
        
        if (userDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(userDTO.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Подразделение не найдено"));
            user.setDepartment(department);
        }
        
        // Обновление пароля только если он указан
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        
        User updatedUser = userRepository.save(user);
        auditService.logAction("UPDATE_USER", "User", updatedUser.getId(), 
                "Обновлен пользователь: " + updatedUser.getUsername());
        
        log.info("Обновлен пользователь: {}", updatedUser.getUsername());
        return convertToDTO(updatedUser);
    }
    
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        user.setDeleted(true);
        user.setActive(false);
        userRepository.save(user);
        
        auditService.logAction("DELETE_USER", "User", id, 
                "Удален пользователь: " + user.getUsername());
        
        log.info("Удален пользователь: {}", user.getUsername());
    }
    
    @Transactional
    public void updateLastLogin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }
    
    private UserDTO convertToDTO(User user) {
        UserDTO dto = UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .lastName(user.getLastName())
                .firstName(user.getFirstName())
                .middleName(user.getMiddleName())
                .role(user.getRole())
                .phone(user.getPhone())
                .position(user.getPosition())
                .active(user.getActive())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .build();
        
        if (user.getDepartment() != null) {
            dto.setDepartmentId(user.getDepartment().getId());
            dto.setDepartmentName(user.getDepartment().getName());
        }
        
        return dto;
    }
}
