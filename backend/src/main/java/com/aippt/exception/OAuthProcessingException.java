package com.aippt.exception;

/**
 * OAuth认证处理异常，当OAuth认证过程出现问题时抛出
 */
public class OAuthProcessingException extends UserException {
    
    public OAuthProcessingException(String message) {
        super(message);
    }
    
    public OAuthProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
} 