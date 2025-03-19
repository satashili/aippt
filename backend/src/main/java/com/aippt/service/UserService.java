package com.aippt.service;

import com.aippt.entity.User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import java.util.Optional;

public interface UserService {
    User createOrUpdateUser(OAuth2User oAuth2User);
    Optional<User> findById(String id);
    boolean updateMemberStatus(String userId, int monthsToAdd);
} 