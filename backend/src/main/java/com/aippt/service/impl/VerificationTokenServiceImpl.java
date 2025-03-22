package com.aippt.service.impl;

import com.aippt.entity.VerificationToken;
import com.aippt.exception.auth.InvalidTokenException;
import com.aippt.exception.auth.TokenExpiredException;
import com.aippt.mapper.VerificationTokenMapper;
import com.aippt.service.VerificationTokenService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 验证Token服务实现类
 */
@Service
public class VerificationTokenServiceImpl extends ServiceImpl<VerificationTokenMapper, VerificationToken> implements VerificationTokenService {
    
    private static final Logger logger = LoggerFactory.getLogger(VerificationTokenServiceImpl.class);
    
    @Value("${app.email.verification.expiration}")
    private long tokenExpirationMinutes;
    
    @Override
    @Transactional
    public VerificationToken createToken(String userId) {
        logger.info("为用户创建验证token: {}", userId);
        
        // 生成随机token
        String token = UUID.randomUUID().toString();
        
        // 计算过期时间 (当前时间 + 配置的过期时间)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(tokenExpirationMinutes);
        
        // 创建token实体
        VerificationToken verificationToken = VerificationToken.builder()
                .userId(userId)
                .token(token)
                .createdAt(now)
                .expiresAt(expiresAt)
                .build();
        
        // 保存到数据库
        this.save(verificationToken);
        
        logger.info("验证token创建成功: {}", token);
        return verificationToken;
    }
    
    @Override
    public VerificationToken findByToken(String token) {
        logger.info("查找验证token: {}", token);
        
        LambdaQueryWrapper<VerificationToken> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VerificationToken::getToken, token);
        
        VerificationToken verificationToken = this.getOne(queryWrapper);
        
        if (verificationToken == null) {
            logger.warn("未找到验证token: {}", token);
        } else {
            logger.info("找到验证token: {}, 用户ID: {}", token, verificationToken.getUserId());
        }
        
        return verificationToken;
    }
    
    @Override
    @Transactional
    public boolean confirmToken(String token) {
        logger.info("确认验证token: {}", token);
        
        // 查找token
        VerificationToken verificationToken = findByToken(token);
        
        // 验证token是否存在
        if (verificationToken == null) {
            logger.warn("无效的验证token: {}", token);
            throw new InvalidTokenException("验证链接无效");
        }
        
        // 检查token是否已经确认过
        if (verificationToken.getConfirmedAt() != null) {
            logger.info("验证token已被确认: {}", token);
            return false;
        }
        
        // 检查token是否已过期
        if (verificationToken.isExpired()) {
            logger.warn("验证token已过期: {}", token);
            throw new TokenExpiredException("验证链接已过期");
        }
        
        // 更新确认时间
        verificationToken.setConfirmedAt(LocalDateTime.now());
        boolean updated = this.updateById(verificationToken);
        
        logger.info("验证token确认结果: {}", updated);
        return updated;
    }
} 