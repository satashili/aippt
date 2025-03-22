package com.aippt.controller;

import com.aippt.exception.InvalidTokenException;
import com.aippt.exception.TokenExpiredException;
import com.aippt.exception.UserNotFoundException;
import com.aippt.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 邮箱验证控制器
 * 处理邮箱验证和重发验证邮件的请求
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*") // 允许跨域请求
public class EmailVerificationController {

    private static final Logger logger = LoggerFactory.getLogger(EmailVerificationController.class);
    
    @Autowired
    private UserService userService;
    
    @Value("${app.email.verification.base-url}")
    private String verificationBaseUrl;
    
    /**
     * 验证邮箱
     * 处理来自验证邮件的链接请求
     *
     * @param token 验证token
     * @return 验证结果
     */
    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        logger.info("收到邮箱验证请求，token: {}", token);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean verified = userService.confirmEmail(token);
            
            if (verified) {
                logger.info("邮箱验证成功: {}", token);
                response.put("success", true);
                response.put("message", "邮箱验证成功，您现在可以登录了");
                return ResponseEntity.ok(response);
            } else {
                logger.warn("邮箱验证失败: {}", token);
                response.put("success", false);
                response.put("message", "邮箱验证失败，可能链接已被使用或无效");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (InvalidTokenException e) {
            logger.error("无效的验证token: {}", token);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (TokenExpiredException e) {
            logger.error("验证token已过期: {}", token);
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("expired", true);
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("验证邮箱时发生错误: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "验证邮箱时发生错误，请稍后重试");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 重新发送验证邮件
     *
     * @param email 用户邮箱
     * @return 发送结果
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerificationEmail(@RequestParam String email) {
        logger.info("收到重新发送验证邮件请求: {}", email);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean sent = userService.resendVerificationEmail(email);
            
            if (sent) {
                logger.info("验证邮件重新发送成功: {}", email);
                response.put("success", true);
                response.put("message", "验证邮件已发送，请查收");
                return ResponseEntity.ok(response);
            } else {
                logger.info("邮箱已验证，无需重新发送: {}", email);
                response.put("success", false);
                response.put("message", "该邮箱已验证，无需重新发送验证邮件");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (UserNotFoundException e) {
            logger.warn("找不到用户: {}", email);
            response.put("success", false);
            response.put("message", "找不到该邮箱关联的用户");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("重新发送验证邮件时发生错误: {}, {}", email, e.getMessage());
            response.put("success", false);
            response.put("message", "发送验证邮件时发生错误，请稍后重试");
            return ResponseEntity.badRequest().body(response);
        }
    }
} 