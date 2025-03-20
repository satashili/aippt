package com.aippt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户数据传输对象
 * 用于在控制器和客户端之间传递用户信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String id;
    private String name;
    private String email;
    private String picture;
    private String givenName;
    private String familyName;
    private Boolean emailVerified;
    private Boolean isMember;
    private LocalDateTime memberExpireTime;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private String authProvider;  // LOCAL, GOOGLE, BOTH
    
    // 不包含敏感字段如密码、externalId等
} 