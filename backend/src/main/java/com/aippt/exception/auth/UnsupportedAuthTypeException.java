package com.aippt.exception.auth;

/**
 * 不支持的认证类型异常
 * 当尝试将不支持的认证类型升级为其他类型时抛出
 */
public class UnsupportedAuthTypeException extends RuntimeException {
    
    public UnsupportedAuthTypeException(String message) {
        super(message);
    }
    
    public UnsupportedAuthTypeException(String message, Throwable cause) {
        super(message, cause);
    }
} 