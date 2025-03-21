package com.aippt.service.impl;

import com.aippt.entity.User;
import com.aippt.exception.OAuthProcessingException;
import com.aippt.exception.UserAlreadyExistsException;
import com.aippt.exception.UserNotFoundException;
import com.aippt.exception.UnsupportedAuthTypeException;
import com.aippt.exception.InvalidPasswordException;
import com.aippt.mapper.UserMapper;
import com.aippt.service.UserService;
import com.aippt.util.PasswordUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * 用户服务实现类
 * 提供用户注册、登录、信息更新等功能
 * 支持本地注册和Google OAuth2登录两种认证方式
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 根据用户ID查询用户信息
     *
     * @param id 用户ID
     * @return 用户信息封装为Optional，如果不存在则为empty
     */
    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(this.getById(id));
    }

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
    @Override
    @Transactional
    public boolean updateUserMembership(String userId, boolean isMember, LocalDateTime expireTime) {
        User existingUser = this.getById(userId);
        if (existingUser == null) {
            log.error("用户不存在: {}", userId);
            throw new UserNotFoundException("用户不存在: " + userId);
        }

        User updatedUser = buildUserWithUpdatedFields(existingUser, builder -> {
            builder.isMember(isMember).memberExpireTime(expireTime);
        });

        log.info("更新用户会员状态: {}, 会员状态: {}, 到期时间: {}", userId, isMember, expireTime);
        return this.updateById(updatedUser);
    }
    
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
     * @throws UserAlreadyExistsException 当邮箱已存在且不是Google用户时抛出
     */
    @Override
    @Transactional
    public User register(String email, String name, String password) {
        // 检查邮箱是否已存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, email);
        User existingUser = this.getOne(queryWrapper);
        
        if (existingUser != null) {
            // 如果用户已存在，检查其认证类型
            if ("GOOGLE".equals(existingUser.getAuthProvider())) {
                // 用户已通过 Google 登录，升级为 BOTH 类型
                log.info("Google用户设置密码，升级为双重认证: {}", email);
                return upgradeUserToBoth(existingUser, password, name, null, null);
            } else {
                // 用户已通过其他方式注册，抛出异常
                log.warn("邮箱已被注册: {}", email);
                throw new UserAlreadyExistsException("邮箱已被注册");
            }
        }
        
        // 用户不存在，创建新用户
        String userId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        // 加密密码
        String encodedPassword = PasswordUtils.processPassword(password);
        
        // 创建新用户
        User newUser = User.builder()
                .id(userId)
                .email(email)
                .name(name)
                .password(encodedPassword)  // 存储加密后的密码
                .emailVerified(false)
                .isMember(false)
                .memberExpireTime(null)
                .createdAt(now)
                .lastLogin(now)
                .authProvider("LOCAL")
                .externalId(null)
                .deleted(0)
                .build();
        
        log.info("注册新用户: ID={}, Email={}, CreatedAt={}", newUser.getId(), newUser.getEmail(), newUser.getCreatedAt());
        this.save(newUser);
        
        return newUser;
    }

    /**
     * 将用户升级为双重认证用户(BOTH)
     * 该方法可以处理两种升级场景：
     * 1. LOCAL用户添加Google认证
     * 2. GOOGLE用户添加本地密码认证
     *
     * @param user 现有用户
     * @param password 新密码(从GOOGLE升级到BOTH时需要)
     * @param name 用户名(从GOOGLE升级到BOTH时可选)
     * @param googleId Google用户ID(从LOCAL升级到BOTH时需要)
     * @param oAuth2User Google用户信息(从LOCAL升级到BOTH时需要)
     * @return 更新后的用户
     * @throws UnsupportedAuthTypeException 当用户认证类型不支持升级时抛出
     */
    private User upgradeUserToBoth(User user, String password, String name, String googleId, OAuth2User oAuth2User) {
        LocalDateTime now = LocalDateTime.now();
        
        // 根据用户类型处理不同的升级场景
        if ("LOCAL".equals(user.getAuthProvider())) {
            // LOCAL → BOTH：本地用户添加Google认证
            log.info("本地用户升级为双重认证: {}", user.getEmail());
            
            // 创建一个更新条件
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getId, user.getId());
            
            // 首先获取最新的用户数据
            User latestUser = this.getOne(queryWrapper);
            if (latestUser == null) {
                throw new UserNotFoundException("用户不存在: " + user.getId());
            }
            
            // 创建更新后的用户对象
            User updatedUser = new User();
            updatedUser.setId(latestUser.getId());
            updatedUser.setEmail(latestUser.getEmail());
            updatedUser.setPassword(latestUser.getPassword());
            updatedUser.setName(oAuth2User.getAttribute("name"));
            updatedUser.setPicture(oAuth2User.getAttribute("picture"));
            updatedUser.setGivenName(oAuth2User.getAttribute("given_name"));
            updatedUser.setFamilyName(oAuth2User.getAttribute("family_name"));
            updatedUser.setEmailVerified(true);
            updatedUser.setIsMember(latestUser.getIsMember());
            updatedUser.setMemberExpireTime(latestUser.getMemberExpireTime());
            updatedUser.setCreatedAt(latestUser.getCreatedAt());
            updatedUser.setLastLogin(now);
            updatedUser.setAuthProvider("BOTH");
            updatedUser.setExternalId(googleId);
            // 移除 version 字段的设置
            updatedUser.setDeleted(latestUser.getDeleted());
            
            // 使用 Mapper 直接更新，绕过乐观锁
            getBaseMapper().updateById(updatedUser);
            
            return updatedUser;
            
        } else if ("GOOGLE".equals(user.getAuthProvider())) {
            // GOOGLE → BOTH：Google用户添加本地密码
            log.info("Google用户设置密码，升级为双重认证: {}", user.getEmail());
            
            // 创建一个更新条件
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getId, user.getId());
            
            // 首先获取最新的用户数据
            User latestUser = this.getOne(queryWrapper);
            if (latestUser == null) {
                throw new UserNotFoundException("用户不存在: " + user.getId());
            }
            
            // 确定用户名
            final String userName = latestUser.getName() != null && !latestUser.getName().isEmpty() ? 
                latestUser.getName() : (name != null && !name.isEmpty() ? name : latestUser.getEmail());
            
            // 加密密码
            String encodedPassword = PasswordUtils.encodePassword(password);
            
            // 创建更新后的用户对象
            User updatedUser = new User();
            updatedUser.setId(latestUser.getId());
            updatedUser.setEmail(latestUser.getEmail());
            updatedUser.setPassword(encodedPassword);
            updatedUser.setName(userName);
            updatedUser.setPicture(latestUser.getPicture());
            updatedUser.setGivenName(latestUser.getGivenName());
            updatedUser.setFamilyName(latestUser.getFamilyName());
            updatedUser.setEmailVerified(latestUser.getEmailVerified());
            updatedUser.setIsMember(latestUser.getIsMember());
            updatedUser.setMemberExpireTime(latestUser.getMemberExpireTime());
            updatedUser.setCreatedAt(latestUser.getCreatedAt());
            updatedUser.setLastLogin(now);
            updatedUser.setAuthProvider("BOTH");
            updatedUser.setExternalId(latestUser.getExternalId());
            // 移除 version 字段的设置
            updatedUser.setDeleted(latestUser.getDeleted());
            
            // 使用 Mapper 直接更新，绕过乐观锁
            getBaseMapper().updateById(updatedUser);
            
            return updatedUser;
        } else {
            // 不支持的认证类型
            log.warn("不支持从{}类型升级为BOTH", user.getAuthProvider());
            throw new UnsupportedAuthTypeException("不支持从" + user.getAuthProvider() + "类型升级为BOTH");
        }
    }

    /**
     * 获取系统中的用户总数
     *
     * @return 用户数量
     */
    @Override
    public long getUserCount() {
        return this.count();
    }

    /**
     * 处理Google OAuth2登录
     * 根据用户是否存在及认证类型，执行不同的处理：
     * 1. 用户不存在：创建新的Google用户
     * 2. 用户存在且为LOCAL：升级为BOTH类型
     * 3. 用户存在且为GOOGLE/BOTH：更新Google信息
     *
     * @param oAuth2User Google认证返回的用户信息
     * @return 处理后的用户对象
     * @throws OAuthProcessingException 当Google登录信息不完整时抛出
     */
    @Override
    @Transactional
    public User processGoogleLogin(OAuth2User oAuth2User) {
        String sub = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        
        if (sub == null || email == null) {
            log.error("Google登录信息不完整: sub={}, email={}", sub, email);
            throw new OAuthProcessingException("Google登录信息不完整");
        }
        
        // 通过邮箱查找用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, email);
        User user = this.getOne(queryWrapper);
        
        LocalDateTime now = LocalDateTime.now();
        
        if (user == null) {
            // 用户不存在，创建新用户
            return createGoogleUser(sub, oAuth2User);
        } else {
            // 用户存在，根据认证类型处理
            if ("LOCAL".equals(user.getAuthProvider())) {
                return upgradeUserToBoth(user, null, null, sub, oAuth2User);
            } else {
                return updateGoogleUser(user, sub, oAuth2User);
            }
        }
    }
    
    /**
     * 创建新的Google用户
     * 从OAuth2User中提取用户信息，创建一个新的GOOGLE类型用户
     *
     * @param sub Google用户ID (Subject)
     * @param oAuth2User Google返回的用户信息
     * @return 创建的新用户
     */
    private User createGoogleUser(String sub, OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String userId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        User newUser = User.builder()
                .id(userId)
                .email(email)
                .name(oAuth2User.getAttribute("name"))
                .password(null)  // Google用户没有密码
                .picture(oAuth2User.getAttribute("picture"))
                .givenName(oAuth2User.getAttribute("given_name"))
                .familyName(oAuth2User.getAttribute("family_name"))
                .emailVerified(true)  // Google已验证邮箱
                .isMember(false)
                .memberExpireTime(null)
                .createdAt(now)
                .lastLogin(now)
                .authProvider("GOOGLE")
                .externalId(sub)
                .deleted(0)
                .build();
        
        log.info("创建Google用户: {}", newUser.getEmail());
        this.save(newUser);
        
        return newUser;
    }
    
    /**
     * 更新Google用户信息
     * 适用于已经是GOOGLE或BOTH类型的用户再次通过Google登录时
     * 
     * @param user 当前用户
     * @param sub Google用户ID
     * @param oAuth2User Google返回的用户信息
     * @return 更新后的用户
     */
    private User updateGoogleUser(User user, String sub, OAuth2User oAuth2User) {
        LocalDateTime now = LocalDateTime.now();
        
        // 更新用户认证相关信息
        User updatedUser = buildUserWithUpdatedFields(user, builder -> {
            // 检查认证类型是否正确
            if (!"BOTH".equals(user.getAuthProvider()) && !"GOOGLE".equals(user.getAuthProvider())) {
                log.warn("用户认证类型不匹配: {}, authProvider={}", user.getEmail(), user.getAuthProvider());
                builder.authProvider("GOOGLE");
            }
            
            // 检查Google ID是否匹配
            if (!sub.equals(user.getExternalId())) {
                log.warn("用户Google ID不匹配: {}, oldId={}, newId={}", user.getEmail(), user.getExternalId(), sub);
                builder.externalId(sub);
            }
            
            // 更新登录时间和Google用户信息
            builder.lastLogin(now)
                   .name(oAuth2User.getAttribute("name"))
                   .picture(oAuth2User.getAttribute("picture"))
                   .givenName(oAuth2User.getAttribute("given_name"))
                   .familyName(oAuth2User.getAttribute("family_name"))
                   .emailVerified(true);
        });
        
        log.info("更新Google用户信息: {}", user.getEmail());
        this.updateById(updatedUser);
        
        return updatedUser;
    }
    
    /**
     * 使用构建器模式更新用户部分字段
     * 保留用户的大部分属性不变，只更新指定的字段
     *
     * @param user 原用户对象
     * @param consumer 接收User.Builder并进行修改的函数式接口
     * @return 更新后的用户对象
     */
    private User buildUserWithUpdatedFields(User user, UserBuilderConsumer consumer) {
        User.UserBuilder builder = User.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .password(user.getPassword())
                .picture(user.getPicture())
                .givenName(user.getGivenName())
                .familyName(user.getFamilyName())
                .emailVerified(user.getEmailVerified())
                .isMember(user.getIsMember())
                .memberExpireTime(user.getMemberExpireTime())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .authProvider(user.getAuthProvider())
                .externalId(user.getExternalId())
                // 移除 version 字段
                .deleted(user.getDeleted());
        
        consumer.accept(builder);
        
        return builder.build();
    }
    
    /**
     * 用于更新用户字段的函数式接口
     * 接收一个User.Builder，对其进行修改后返回
     */
    @FunctionalInterface
    private interface UserBuilderConsumer {
        /**
         * 接收并操作User.Builder
         * 
         * @param builder 用户构建器
         */
        void accept(User.UserBuilder builder);
    }

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
    @Override
    @Transactional
    public User login(String email, String password) {
        log.info("用户登录: {}", email);
        
        // 查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, email);
        User user = this.getOne(queryWrapper);
        
        // 检查用户是否存在
        if (user == null) {
            log.warn("登录失败: 用户不存在 {}", email);
            throw new UserNotFoundException("用户不存在或邮箱错误");
        }
        
        // 检查用户类型是否支持密码登录
        if ("GOOGLE".equals(user.getAuthProvider())) {
            log.warn("登录失败: Google用户尝试使用密码登录 {}", email);
            throw new InvalidPasswordException("该账号是通过Google注册的，请使用Google登录");
        }
        
        // 验证密码
        try {
            PasswordUtils.validatePassword(password, user.getPassword());
        } catch (InvalidPasswordException e) {
            log.warn("登录失败: 密码错误 {}", email);
            throw e; // 重新抛出异常
        }
        
        // 更新最后登录时间
        LocalDateTime now = LocalDateTime.now();
        User updatedUser = buildUserWithUpdatedFields(user, builder -> {
            builder.lastLogin(now);
        });
        
        this.updateById(updatedUser);
        
        log.info("用户登录成功: {}", email);
        return updatedUser;
    }
}