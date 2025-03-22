package com.aippt.controller;

import com.aippt.entity.User;
import com.aippt.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @GetMapping("/user")
    public Map<String, Object> user(@AuthenticationPrincipal OAuth2User principal, HttpServletRequest request) {
        Map<String, Object> userInfo = new HashMap<>();
        
        // 尝试获取OAuth2用户信息（Google登录）
        if (principal != null) {
            logger.info("==================== Google登录用户信息 ====================");
            logger.info("原始认证信息: {}", principal.getAttributes());
            
            try {
                // 处理 Google 登录，获取或创建用户
                User user = userService.processGoogleLogin(principal);
                
                logger.info("处理后的用户信息:");
                logger.info("用户ID: {}", user.getId());
                logger.info("邮箱: {}", user.getEmail());
                logger.info("用户名: {}", user.getName());
                logger.info("认证类型: {}", user.getAuthProvider());
                logger.info("======================================================");
                
                // 返回处理后的用户信息
                userInfo.put("id", user.getId());
                userInfo.put("name", user.getName());
                userInfo.put("email", user.getEmail());
                userInfo.put("picture", user.getPicture());
                userInfo.put("authProvider", user.getAuthProvider());
                userInfo.put("isMember", user.getIsMember());
                userInfo.put("memberExpireTime", user.getMemberExpireTime());
                
                logger.info("用户信息已成功处理并返回");
                return userInfo;
            } catch (Exception e) {
                logger.error("处理Google登录失败", e);
                userInfo.put("error", "处理登录失败: " + e.getMessage());
                return userInfo;
            }
        }
        
        // 尝试获取普通登录的会话信息
        HttpSession session = request.getSession(false);
        if (session != null) {
            String userId = (String) session.getAttribute("userId");
            if (userId != null) {
                logger.info("==================== 普通登录用户信息 ====================");
                logger.info("Session ID: {}", session.getId());
                logger.info("用户ID: {}", userId);
                
                try {
                    // 通过userId获取用户信息
                    Optional<User> userOpt = userService.findById(userId);
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        logger.info("成功获取到用户: {}", user.getEmail());
                        logger.info("======================================================");
                        
                        // 返回用户信息
                        userInfo.put("id", user.getId());
                        userInfo.put("name", user.getName());
                        userInfo.put("email", user.getEmail());
                        userInfo.put("picture", user.getPicture());
                        userInfo.put("authProvider", user.getAuthProvider());
                        userInfo.put("isMember", user.getIsMember());
                        userInfo.put("memberExpireTime", user.getMemberExpireTime());
                        
                        return userInfo;
                    }
                } catch (Exception e) {
                    logger.error("获取普通登录用户信息失败", e);
                    userInfo.put("error", "获取用户信息失败: " + e.getMessage());
                    return userInfo;
                }
            }
        }
        
        // 未找到任何认证信息
        logger.warn("未找到已认证的用户信息");
        userInfo.put("error", "未找到认证信息");
        return userInfo;
    }

    @PostMapping("/logout")
    public Map<String, String> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            logger.info("==================== 用户注销 ====================");
            logger.info("Session ID: {}", session.getId());
            session.invalidate();
            logger.info("Session已成功注销");
            logger.info("======================================================");
        } else {
            logger.warn("未找到活动的会话");
        }
        
        SecurityContextHolder.clearContext();
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Logged out successfully");
        
        return response;
    }
}