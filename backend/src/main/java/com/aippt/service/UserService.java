package com.aippt.service;

import com.aippt.entity.User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import java.util.Optional;

/**
 * 用户服务接口
 * 提供用户相关的业务逻辑操作，包括用户查询、注册、登录和会员管理等功能
 */
public interface UserService {
    
    /**
     * 根据用户ID查询用户信息
     * 
     * @param id 用户ID
     * @return 用户信息，如果不存在则返回空
     */
    Optional<User> findById(String id);
    
    /**
     * 获取系统中的用户总数
     * 
     * @return 用户总数
     */
    long getUserCount();
    
    /**
     * 用户注册
     * 通过邮箱、用户名和密码创建新用户
     * 
     * @param email 用户邮箱
     * @param name 用户名
     * @param password 密码（未加密）
     * @return 注册成功的用户信息
     * @throws RuntimeException 如果邮箱已被注册
     */
    User register(String email, String name, String password);
    
    /**
     * 处理Google OAuth2登录
     * 根据Google返回的用户信息创建或更新用户
     * 
     * @param oAuth2User Google认证后的用户信息
     * @return 处理后的用户信息
     */
    User processGoogleLogin(OAuth2User oAuth2User);
    
    /**
     * 更新用户的会员状态
     * 如果用户已是会员且会员未过期，则在原有期限上延长；否则从当前时间开始计算
     * 
     * @param userId 用户ID
     * @param monthsToAdd 增加的会员月数
     * @return 更新是否成功
     */
    boolean updateMemberStatus(String userId, int monthsToAdd);
}