package com.aippt.service.impl;

import com.aippt.entity.User;
import com.aippt.exception.OAuthProcessingException;
import com.aippt.exception.UserAlreadyExistsException;
import com.aippt.exception.UserNotFoundException;
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

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(this.getById(id));
    }

    @Override
    @Transactional
    public boolean updateMemberStatus(String userId, int monthsToAdd) {
        User existingUser = this.getById(userId);
        if (existingUser == null) {
            log.error("用户不存在: {}", userId);
            throw new UserNotFoundException("用户不存在: " + userId);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime newExpireTime;

        if (existingUser.getMemberExpireTime() != null && existingUser.getMemberExpireTime().isAfter(now)) {
            newExpireTime = existingUser.getMemberExpireTime().plusMonths(monthsToAdd);
        } else {
            newExpireTime = now.plusMonths(monthsToAdd);
        }

        User updatedUser = buildUserWithUpdatedFields(existingUser, builder -> {
            builder.isMember(true).memberExpireTime(newExpireTime);
        });

        log.info("更新用户会员状态: {}, 新到期时间: {}", userId, newExpireTime);
        return this.updateById(updatedUser);
    }
    
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
                return upgradeGoogleUserToLocal(existingUser, password, name);
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
        String encodedPassword = PasswordUtils.encodePassword(password);
        
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
                .version(0)
                .deleted(0)
                .build();
        
        log.info("注册新用户: ID={}, Email={}, CreatedAt={}", newUser.getId(), newUser.getEmail(), newUser.getCreatedAt());
        this.save(newUser);
        
        return newUser;
    }

    /**
     * 将Google用户升级为双重认证用户
     */
    private User upgradeGoogleUserToLocal(User user, String password, String name) {
        String encodedPassword = PasswordUtils.encodePassword(password);
        LocalDateTime now = LocalDateTime.now();
        
        // 如果提供了新名字且原用户名为空，则更新用户名
        String userName = (user.getName() == null || user.getName().isEmpty()) ? name : user.getName();
        
        User updatedUser = User.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(userName)
                .password(encodedPassword)  // 设置密码
                .picture(user.getPicture())
                .givenName(user.getGivenName())
                .familyName(user.getFamilyName())
                .emailVerified(user.getEmailVerified())
                .isMember(user.getIsMember())
                .memberExpireTime(user.getMemberExpireTime())
                .createdAt(user.getCreatedAt())
                .lastLogin(now)
                .authProvider("BOTH")  // 升级为双重认证
                .externalId(user.getExternalId())
                .version(user.getVersion())
                .deleted(user.getDeleted())
                .build();
        
        log.info("Google用户设置密码，升级为双重认证: {}", user.getEmail());
        this.updateById(updatedUser);
        
        return updatedUser;
    }

    @Override
    public long getUserCount() {
        return this.count();
    }

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
                return upgradeToGoogleLogin(user, sub, oAuth2User);
            } else {
                return updateGoogleUser(user, sub, oAuth2User);
            }
        }
    }
    
    /**
     * 创建新的Google用户
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
                .version(0)
                .deleted(0)
                .build();
        
        log.info("创建Google用户: {}", newUser.getEmail());
        this.save(newUser);
        
        return newUser;
    }
    
    /**
     * 将本地用户升级为双重认证用户
     */
    private User upgradeToGoogleLogin(User user, String sub, OAuth2User oAuth2User) {
        LocalDateTime now = LocalDateTime.now();
        
        User updatedUser = updateUserFromOAuth2(user, oAuth2User, now);
        updatedUser = buildUserWithUpdatedFields(updatedUser, builder -> {
            builder.authProvider("BOTH").externalId(sub);
        });
        
        log.info("升级用户为双重认证: {}", user.getEmail());
        this.updateById(updatedUser);
        
        return updatedUser;
    }
    
    /**
     * 更新Google用户信息
     */
    private User updateGoogleUser(User user, String sub, OAuth2User oAuth2User) {
        // 检查externalId是否需要更新
        if (!"BOTH".equals(user.getAuthProvider()) && !"GOOGLE".equals(user.getAuthProvider())) {
            log.warn("用户认证类型不匹配: {}, authProvider={}", user.getEmail(), user.getAuthProvider());
            user.setAuthProvider("GOOGLE");
        }
        
        if (!sub.equals(user.getExternalId())) {
            log.warn("用户Google ID不匹配: {}, oldId={}, newId={}", user.getEmail(), user.getExternalId(), sub);
            user.setExternalId(sub);
        }
        
        LocalDateTime now = LocalDateTime.now();
        User updatedUser = updateUserFromOAuth2(user, oAuth2User, now);
        
        log.info("更新Google用户信息: {}", user.getEmail());
        this.updateById(updatedUser);
        
        return updatedUser;
    }
    
    /**
     * 从OAuth2User更新用户基本信息
     */
    private User updateUserFromOAuth2(User user, OAuth2User oAuth2User, LocalDateTime loginTime) {
        return User.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(oAuth2User.getAttribute("name"))
                .password(user.getPassword())
                .picture(oAuth2User.getAttribute("picture"))
                .givenName(oAuth2User.getAttribute("given_name"))
                .familyName(oAuth2User.getAttribute("family_name"))
                .emailVerified(true)
                .isMember(user.getIsMember())
                .memberExpireTime(user.getMemberExpireTime())
                .createdAt(user.getCreatedAt())
                .lastLogin(loginTime)
                .authProvider(user.getAuthProvider())
                .externalId(user.getExternalId())
                .version(user.getVersion())
                .deleted(user.getDeleted())
                .build();
    }
    
    /**
     * 使用构建器模式更新用户部分字段
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
                .version(user.getVersion())
                .deleted(user.getDeleted());
        
        consumer.accept(builder);
        
        return builder.build();
    }
    
    /**
     * 用于更新用户字段的函数式接口
     */
    @FunctionalInterface
    private interface UserBuilderConsumer {
        void accept(User.UserBuilder builder);
    }
}