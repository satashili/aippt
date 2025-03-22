package com.aippt.exception;

/**
 * 用户已存在异常，当尝试注册已有邮箱时抛出
 */
public class UserAlreadyExistsException extends UserException {
    
    public UserAlreadyExistsException(String message) {
        super(message);
    }
} 