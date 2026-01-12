package com.systemtmc.inventory.controller;

import com.systemtmc.inventory.dto.LoginRequest;
import com.systemtmc.inventory.dto.LoginResponse;
import com.systemtmc.inventory.model.entity.User;
import com.systemtmc.inventory.repository.UserRepository;
import com.systemtmc.inventory.security.JwtTokenProvider;
import com.systemtmc.inventory.security.UserPrincipal;
import com.systemtmc.inventory.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для аутентификации и авторизации
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final UserService userService;
    
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            String jwt = tokenProvider.generateToken(authentication);
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            
            User user = userRepository.findById(userPrincipal.getId())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            
            // Обновление времени последнего входа
            userService.updateLastLogin(user.getId());
            
            LoginResponse response = LoginResponse.builder()
                    .token(jwt)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .role(user.getRole())
                    .build();
            
            if (user.getDepartment() != null) {
                response.setDepartmentId(user.getDepartment().getId());
                response.setDepartmentName(user.getDepartment().getName());
            }
            
            log.info("Пользователь {} успешно аутентифицирован", user.getUsername());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Ошибка аутентификации: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Неверное имя пользователя или пароль");
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            return ResponseEntity.ok(userService.getUserById(userPrincipal.getId()));
        }
        return ResponseEntity.badRequest().body("Пользователь не аутентифицирован");
    }
}
