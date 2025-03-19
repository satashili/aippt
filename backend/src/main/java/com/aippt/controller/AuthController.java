package com.aippt.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @GetMapping("/user")
    public Map<String, Object> user(@AuthenticationPrincipal OAuth2User principal) {
        Map<String, Object> userInfo = new HashMap<>();
        
        if (principal != null) {
            String name = principal.getAttribute("name");
            String email = principal.getAttribute("email");
            String picture = principal.getAttribute("picture");
            
            logger.info("==================== User Information ====================");
            logger.info("Name: {}", name);
            logger.info("Email: {}", email);
            logger.info("Picture URL: {}", picture);
            logger.info("======================================================");
            
            userInfo.put("name", name);
            userInfo.put("email", email);
            userInfo.put("picture", picture);
            
            logger.info("User information successfully retrieved");
        } else {
            logger.warn("No authenticated user found");
        }

        return userInfo;
    }

    @PostMapping("/logout")
    public Map<String, String> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            logger.info("Session found with ID: {}", session.getId());
            session.invalidate();
            logger.info("Session successfully invalidated");
        } else {
            logger.warn("No active session found");
        }
        
        // 清除SecurityContext
        SecurityContextHolder.clearContext();
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Logged out successfully");
        
        logger.info("User logged out successfully");

        return response;
    }
}