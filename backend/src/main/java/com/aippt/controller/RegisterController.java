package com.aippt.controller;

import com.aippt.dto.auth.RegisterRequest;
import com.aippt.entity.User;
import com.aippt.exception.user.UserAlreadyExistsException;
import com.aippt.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
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
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest,
                                     BindingResult bindingResult) {

        Map<String, Object> response = new HashMap<>();

        // 1. 验证请求数据
        if (bindingResult.hasErrors()) {
            logger.info("==================== 验证错误 ====================");
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> {
                logger.info("字段: {}, 错误: {}", error.getField(), error.getDefaultMessage());
                errors.put(error.getField(), error.getDefaultMessage());
            });
            logger.info("======================================================");
            
            response.put("success", false);
            response.put("errors", errors);
            response.put("message", "输入数据验证失败");
            return ResponseEntity.badRequest().body(response);
        }

        // 2. 注册用户
        try {
            User user = userService.register(
                registerRequest.getEmail(),
                registerRequest.getName(),
                registerRequest.getPassword()
            );

            response.put("success", true);
            response.put("message", "Registration successful");
            response.put("userId", user.getId());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("==================== 注册失败 ====================");
            logger.error("错误信息: {}", e.getMessage());
            logger.error("错误详情: ", e);
            logger.error("======================================================");
            
            // 增强错误消息
            response.put("success", false);
            
            // 处理不同类型的错误
            String errorMessage = e.getMessage();
            if (e instanceof UserAlreadyExistsException) {
                errorMessage = "该邮箱已被注册，请直接登录或使用其他邮箱";
                response.put("errorType", "USER_EXISTS");
            } else if (errorMessage == null || errorMessage.trim().isEmpty()) {
                // 如果没有错误消息，提供一个通用消息
                errorMessage = "注册过程中发生错误，请稍后重试";
                response.put("errorType", "INTERNAL_ERROR");
            }
            
            response.put("message", errorMessage);
            
            // 添加错误详情（如果适用）
            Map<String, String> errorDetails = new HashMap<>();
            if (e.getCause() != null) {
                errorDetails.put("cause", e.getCause().getMessage());
            }
            
            if (!errorDetails.isEmpty()) {
                response.put("errorDetails", errorDetails);
            }
            
            return ResponseEntity.badRequest().body(response);
        }
    }
}