package com.aippt.controller;

import com.aippt.dto.auth.LoginRequest;
import com.aippt.entity.User;
import com.aippt.exception.InvalidPasswordException;
import com.aippt.exception.UserNotFoundException;
import com.aippt.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest,
                                  BindingResult bindingResult,
                                  HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();

        // 检查验证错误
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

        try {
            // 调用登录服务
            User user = userService.login(
                loginRequest.getEmail(),
                loginRequest.getPassword()
            );

            // 创建或更新session
            HttpSession session = request.getSession(true);
            session.setAttribute("userId", user.getId());
            session.setAttribute("email", user.getEmail());
            session.setAttribute("authProvider", user.getAuthProvider());
            
            logger.info("==================== 登录成功 ====================");
            logger.info("用户ID: {}", user.getId());
            logger.info("邮箱: {}", user.getEmail());
            logger.info("认证类型: {}", user.getAuthProvider());
            logger.info("Session ID: {}", session.getId());
            logger.info("======================================================");

            // 返回成功响应
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("userId", user.getId());
            response.put("name", user.getName());
            response.put("email", user.getEmail());
            response.put("picture", user.getPicture());
            response.put("authProvider", user.getAuthProvider());
            response.put("isMember", user.getIsMember());
            response.put("memberExpireTime", user.getMemberExpireTime());

            return ResponseEntity.ok(response);
            
        } catch (UserNotFoundException e) {
            logger.warn("==================== 登录失败: 用户不存在 ====================");
            logger.warn("邮箱: {}", loginRequest.getEmail());
            logger.warn("错误信息: {}", e.getMessage());
            logger.warn("======================================================");
            
            response.put("success", false);
            response.put("errorType", "USER_NOT_FOUND");
            response.put("message", "用户不存在或邮箱错误");
            
            return ResponseEntity.badRequest().body(response);
            
        } catch (InvalidPasswordException e) {
            logger.warn("==================== 登录失败: 密码错误 ====================");
            logger.warn("邮箱: {}", loginRequest.getEmail());
            logger.warn("错误信息: {}", e.getMessage());
            logger.warn("======================================================");
            
            response.put("success", false);
            response.put("errorType", "INVALID_PASSWORD");
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            logger.error("==================== 登录异常 ====================");
            logger.error("邮箱: {}", loginRequest.getEmail());
            logger.error("错误信息: {}", e.getMessage());
            logger.error("错误详情: ", e);
            logger.error("======================================================");
            
            response.put("success", false);
            response.put("errorType", "SERVER_ERROR");
            response.put("message", "登录过程中发生错误，请稍后重试");
            
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