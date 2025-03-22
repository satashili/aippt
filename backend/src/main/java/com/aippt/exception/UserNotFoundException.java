package com.aippt.exception;

/**
 * 用户不存在异常，当查询的用户ID不存在时抛出
 */
public class UserNotFoundException extends UserException {
    
    public UserNotFoundException(String message) {
        super(message);
    }
} 