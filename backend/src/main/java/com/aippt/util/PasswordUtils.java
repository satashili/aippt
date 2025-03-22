package com.aippt.util;

import com.aippt.exception.user.InvalidPasswordException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 密码工具类 - 封装所有密码相关操作
 */
public class PasswordUtils {
    
    private static final Logger log = LoggerFactory.getLogger(PasswordUtils.class);
    
    // 创建BCryptPasswordEncoder实例，而不是通过依赖注入
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * 加密密码
     * 
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public static String encodePassword(String rawPassword) {
        if (rawPassword == null) {
            return null;
        }
        String encoded = passwordEncoder.encode(rawPassword);
        log.debug("密码加密: 原始密码长度={}, 加密后密码长度={}", 
                 (rawPassword != null ? rawPassword.length() : 0), 
                 (encoded != null ? encoded.length() : 0));
        return encoded;
    }
    
    /**
     * 验证密码
     * 
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matchPassword(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            log.warn("密码验证失败: 原始密码或加密密码为null");
            return false;
        }
        
        try {
            boolean matches =  passwordEncoder.matches(rawPassword, encodedPassword);
            log.debug("密码验证结果: {}, 原始密码长度={}, 加密密码长度={}", 
                      matches, rawPassword.length(), encodedPassword.length());
            return matches;
        } catch (Exception e) {
            log.error("密码验证异常: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 验证密码并在不匹配时抛出异常
     * 
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @throws InvalidPasswordException 如果密码不匹配
     */
    public static void validatePassword(String rawPassword, String encodedPassword) {
        log.debug("验证密码: 原始密码长度={}, 加密密码长度={}", 
                 (rawPassword != null ? rawPassword.length() : 0), 
                 (encodedPassword != null ? encodedPassword.length() : 0));
        
        if (!matchPassword(rawPassword, encodedPassword)) {
            throw new InvalidPasswordException("密码不正确");
        }
    }
} 