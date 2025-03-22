package com.aippt.service.impl;

import com.aippt.entity.User;
import com.aippt.exception.UserNotFoundException;
import com.aippt.service.MembershipService;
import com.aippt.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 会员服务实现类
 */
@Slf4j
@Service
public class MembershipServiceImpl implements MembershipService {

    @Autowired
    private UserService userService;
    
    @Override
    @Transactional
    public boolean updateMemberStatus(String userId, int monthsToAdd) {
        if (monthsToAdd <= 0) {
            log.warn("会员月数必须大于0: {}", monthsToAdd);
            return false;
        }
        
        User user = userService.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("用户不存在: " + userId));
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime newExpireTime;
        
        // 如果用户已是会员且会员未过期，在原有期限上延长
        if (user.getMemberExpireTime() != null && user.getMemberExpireTime().isAfter(now)) {
            newExpireTime = user.getMemberExpireTime().plusMonths(monthsToAdd);
            log.info("延长会员期限: {}, 原到期时间: {}, 新到期时间: {}", 
                    userId, user.getMemberExpireTime(), newExpireTime);
        } else {
            // 否则从当前时间开始计算
            newExpireTime = now.plusMonths(monthsToAdd);
            log.info("设置新会员: {}, 到期时间: {}", userId, newExpireTime);
        }
        
        // 更新会员状态
        return userService.updateUserMembership(userId, true, newExpireTime);
    }
    
    @Override
    public boolean isActiveMember(String userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("用户不存在: " + userId));
        
        LocalDateTime now = LocalDateTime.now();
        
        // 检查用户是否是会员且会员未过期
        return user.getIsMember() != null && 
               user.getIsMember() && 
               user.getMemberExpireTime() != null && 
               user.getMemberExpireTime().isAfter(now);
    }
} 