package com.aippt.exception.auth;

/**
 * 当验证token已过期时抛出此异常
 */
public class TokenExpiredException extends RuntimeException {
    
    public TokenExpiredException(String message) {
        super(message);
    }
    
    public TokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
} 