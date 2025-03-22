package com.aippt.util;

import com.aippt.exception.InvalidPasswordException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码工具类 - 封装所有密码相关操作
 */
public class PasswordUtils {
    
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
        return passwordEncoder.encode(rawPassword);
    }
    
    /**
     * 处理密码加密，如果密码为空则返回null
     * 主要用于用户更新场景
     * 
     * @param password 原始密码，可能为null
     * @return 加密后的密码或null
     */
    public static String processPassword(String password) {
        return password != null ? encodePassword(password) : null;
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
            return false;
        }
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    
    /**
     * 验证密码并在不匹配时抛出异常
     * 
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @throws InvalidPasswordException 如果密码不匹配
     */
    public static void validatePassword(String rawPassword, String encodedPassword) {
        if (!matchPassword(rawPassword, encodedPassword)) {
            throw new InvalidPasswordException("密码不正确");
        }
    }
} 