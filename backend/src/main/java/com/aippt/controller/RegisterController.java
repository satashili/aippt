package com.aippt.controller;

import com.aippt.dto.auth.RegisterRequest;
import com.aippt.entity.User;
import com.aippt.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class RegisterController {

    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        logger.info("Received registration request for: {}", registerRequest.getEmail());

        Map<String, Object> response = new HashMap<>();

        try {
            User user = userService.register(
                registerRequest.getEmail(),
                registerRequest.getName(),
                registerRequest.getPassword()
            );

            response.put("success", true);
            response.put("message", "Registration successful");
            response.put("userId", user.getId());

            logger.info("User registered successfully: {}", user.getEmail());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // 处理运行时异常（如用户已存在、密码不符合要求等）
            logger.error("Registration failed: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            // 处理其他异常（如数据库连接失败等）
            logger.error("Registration failed for user: {}", registerRequest.getEmail(), e);
            response.put("success", false);
            response.put("message", "An unexpected error occurred");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}