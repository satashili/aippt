package com.aippt.service;

import com.aippt.entity.User;
import com.aippt.exception.user.UserNotFoundException;
import com.aippt.exception.user.InvalidPasswordException;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 用户服务接口
 * 定义用户相关的业务逻辑接口
 */
public interface UserService extends IService<User> {

    /**
     * 根据用户ID查询用户信息
     *
     * @param id 用户ID
     * @return 用户信息封装为Optional，如果不存在则为empty
     */
    Optional<User> findById(String id);

    /**
     * 更新用户的会员属性
     * 仅负责更新用户实体的会员相关字段，不包含业务逻辑判断
     *
     * @param userId 要更新的用户ID
     * @param isMember 是否为会员
     * @param expireTime 会员到期时间
     * @return 更新是否成功
     * @throws UserNotFoundException 当用户ID不存在时抛出
     */
    boolean updateUserMembership(String userId, boolean isMember, LocalDateTime expireTime);

    /**
     * 用户注册方法
     * 支持两种情况：
     * 1. 全新用户的本地注册
     * 2. 已有Google用户设置密码升级为双重认证(BOTH)
     *
     * @param email 用户邮箱
     * @param name 用户名
     * @param password 用户密码（未加密）
     * @return 注册成功的用户对象
     */
    User register(String email, String name, String password);

    /**
     * 获取系统中的用户总数
     *
     * @return 用户数量
     */
    long getUserCount();

    /**
     * 处理Google OAuth2登录
     * 根据用户是否存在及认证类型，执行不同的处理
     *
     * @param oAuth2User Google认证返回的用户信息
     * @return 处理后的用户对象
     */
    User processGoogleLogin(OAuth2User oAuth2User);

    /**
     * 用户登录方法
     * 验证用户邮箱和密码，更新最后登录时间
     *
     * @param email 用户邮箱
     * @param password 用户密码（未加密）
     * @return 登录成功的用户信息
     * @throws UserNotFoundException 当用户不存在时抛出
     * @throws InvalidPasswordException 当密码不正确时抛出
     */
    User login(String email, String password);

    /**
     * 确认用户邮箱
     * 根据验证token确认用户邮箱的有效性
     *
     * @param token 验证token
     * @return 确认结果
     */
    boolean confirmEmail(String token);
    
    /**
     * 重新发送验证邮件
     * 为已注册但未验证邮箱的用户重新发送验证邮件
     *
     * @param email 用户邮箱
     * @return 是否成功发送
     * @throws UserNotFoundException 当用户不存在时抛出
     */
    boolean resendVerificationEmail(String email);
}