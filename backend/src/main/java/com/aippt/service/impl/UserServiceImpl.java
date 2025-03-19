//package com.aippt.service.impl;
//
//import com.aippt.entity.User;
//import com.aippt.mapper.UserMapper;
//import com.aippt.service.UserService;
//import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//@Slf4j
//@Service
//public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
//
//    @Override
//    @Transactional
//    public User createOrUpdateUser(OAuth2User oAuth2User) {
//        String sub = oAuth2User.getAttribute("sub");
//        User user = this.getById(sub);
//
//        if (user == null) {
//            user = new User();
//            user.setId(sub);
//            user.setCreatedAt(LocalDateTime.now());
//            user.setIsMember(false);
//        }
//
//        user.setName(oAuth2User.getAttribute("name"));
//        user.setEmail(oAuth2User.getAttribute("email"));
//        user.setPicture(oAuth2User.getAttribute("picture"));
//        user.setGivenName(oAuth2User.getAttribute("given_name"));
//        user.setFamilyName(oAuth2User.getAttribute("family_name"));
//        user.setEmailVerified(oAuth2User.getAttribute("email_verified"));
//        user.setLastLogin(LocalDateTime.now());
//
//        log.info("Saving/Updating user information: {}", user);
//        this.saveOrUpdate(user);
//
//        return user;
//    }
//
//    @Override
//    public Optional<User> findById(String id) {
//        return Optional.ofNullable(this.getById(id));
//    }
//
//    @Override
//    @Transactional
//    public boolean updateMemberStatus(String userId, int monthsToAdd) {
//        User user = this.getById(userId);
//        if (user == null) {
//            log.error("User not found with id: {}", userId);
//            return false;
//        }
//
//        LocalDateTime now = LocalDateTime.now();
//        LocalDateTime newExpireTime;
//
//        if (user.getMemberExpireTime() != null && user.getMemberExpireTime().isAfter(now)) {
//            newExpireTime = user.getMemberExpireTime().plusMonths(monthsToAdd);
//        } else {
//            newExpireTime = now.plusMonths(monthsToAdd);
//        }
//
//        user.setIsMember(true);
//        user.setMemberExpireTime(newExpireTime);
//
//        log.info("Updating member status for user: {}, new expire time: {}", userId, newExpireTime);
//        return this.updateById(user);
//    }
//}