package com.aippt.exception.user;

/**
 * 密码无效异常，当用户密码验证失败时抛出
 */
public class InvalidPasswordException extends UserException {
    
    public InvalidPasswordException(String message) {
        super(message);
    }
} 