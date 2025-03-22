package com.aippt.exception.user;

/**
 * 用户相关操作的基础异常类
 */
public class UserException extends RuntimeException {
    
    public UserException(String message) {
        super(message);
    }
    
    public UserException(String message, Throwable cause) {
        super(message, cause);
    }
} 