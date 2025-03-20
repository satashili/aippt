package com.aippt.util;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码工具类
 */
public class PasswordUtils {
    
    /**
     * 加密密码
     * 
     * @param rawPassword 原始密码
     * @param passwordEncoder 密码编码器
     * @return 加密后的密码
     */
    public static String encodePassword(String rawPassword, PasswordEncoder passwordEncoder) {
        if (rawPassword == null) {
            return null;
        }
        return passwordEncoder.encode(rawPassword);
    }
    
    /**
     * 验证密码
     * 
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @param passwordEncoder 密码编码器
     * @return 是否匹配
     */
    public static boolean matchPassword(String rawPassword, String encodedPassword, PasswordEncoder passwordEncoder) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
} 