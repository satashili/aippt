package com.aippt.exception.common;

import com.aippt.exception.auth.InvalidTokenException;
import com.aippt.exception.auth.OAuthProcessingException;
import com.aippt.exception.auth.TokenExpiredException;
import com.aippt.exception.payment.PaymentProcessingException;
import com.aippt.exception.user.InvalidPasswordException;
import com.aippt.exception.user.UserAlreadyExistsException;
import com.aippt.exception.user.UserException;
import com.aippt.exception.user.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理用户已存在异常
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        log.warn("用户已存在: {}", ex.getMessage());
        
        Map<String, String> response = new HashMap<>();
        response.put("error", "user_already_exists");
        response.put("message", ex.getMessage());
        
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }
    
    /**
     * 处理密码无效异常
     */
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPasswordException(InvalidPasswordException ex) {
        log.warn("密码验证失败: {}", ex.getMessage());
        
        Map<String, String> response = new HashMap<>();
        response.put("error", "invalid_password");
        response.put("message", ex.getMessage());
        
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
    
    /**
     * 处理用户不存在异常
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFoundException(UserNotFoundException ex) {
        log.warn("用户不存在: {}", ex.getMessage());
        
        Map<String, String> response = new HashMap<>();
        response.put("error", "user_not_found");
        response.put("message", ex.getMessage());
        
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
    
    /**
     * 处理OAuth处理异常
     */
    @ExceptionHandler(OAuthProcessingException.class)
    public ResponseEntity<Map<String, String>> handleOAuthProcessingException(OAuthProcessingException ex) {
        log.error("OAuth处理错误: {}", ex.getMessage());
        
        Map<String, String> response = new HashMap<>();
        response.put("error", "oauth_processing_error");
        response.put("message", ex.getMessage());
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * 处理通用用户异常
     */
    @ExceptionHandler(UserException.class)
    public ResponseEntity<Map<String, String>> handleUserException(UserException ex) {
        log.error("用户操作错误: {}", ex.getMessage());
        
        Map<String, String> response = new HashMap<>();
        response.put("error", "user_operation_error");
        response.put("message", ex.getMessage());
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * 处理其他未分类异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        log.error("系统错误: {}", ex.getMessage(), ex);
        
        Map<String, String> response = new HashMap<>();
        response.put("error", "internal_server_error");
        response.put("message", "服务器内部错误");
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
} 