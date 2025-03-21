package com.aippt.service;

import com.aippt.entity.VerificationToken;

/**
 * 验证Token服务接口
 */
public interface VerificationTokenService {
    
    /**
     * 创建新的验证token
     *
     * @param userId 用户ID
     * @return 创建的验证token
     */
    VerificationToken createToken(String userId);
    
    /**
     * 通过token查找验证记录
     *
     * @param token 验证token
     * @return 验证记录，如不存在则返回null
     */
    VerificationToken findByToken(String token);
    
    /**
     * 确认验证token
     *
     * @param token 验证token
     * @return 是否成功确认
     */
    boolean confirmToken(String token);
} 