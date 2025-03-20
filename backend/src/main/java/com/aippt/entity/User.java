package com.aippt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("users")
public class User {
    
    @TableId(type = IdType.INPUT)
    private String id;
    
    private String name;
    private String email;
    private String picture;
    
    @TableField(value = "password", select = false)
    private String password;
    
    @TableField("given_name")
    private String givenName;
    
    @TableField("family_name")
    private String familyName;
    
    @TableField("email_verified")
    private Boolean emailVerified;
    
    @TableField("is_member")
    private Boolean isMember;
    
    @TableField("member_expire_time")
    private LocalDateTime memberExpireTime;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("last_login")
    private LocalDateTime lastLogin;
    
    @Version
    private Integer version;
    
    @TableLogic
    private Integer deleted;
} 