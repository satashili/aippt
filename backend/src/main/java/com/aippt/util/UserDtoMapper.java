package com.aippt.util;

import com.aippt.dto.UserDTO;
import com.aippt.entity.User;
import org.springframework.stereotype.Component;

/**
 * 用户实体与DTO转换工具类
 */
@Component("userDtoMapper")
public class UserDtoMapper {
    
    /**
     * 将User实体转换为UserDTO
     * 
     * @param user 用户实体
     * @return 用户DTO
     */
    public UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }
        
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .picture(user.getPicture())
                .givenName(user.getGivenName())
                .familyName(user.getFamilyName())
                .emailVerified(user.getEmailVerified())
                .isMember(user.getIsMember())
                .memberExpireTime(user.getMemberExpireTime())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .authProvider(user.getAuthProvider())
                .build();
    }
} 