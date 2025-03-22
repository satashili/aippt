package com.aippt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 邮箱验证Token实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("verification_token")
public class VerificationToken {
    
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    
    private String token;
    
    private String userId;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime expiresAt;
    
    private LocalDateTime confirmedAt;
    
    /**
     * 检查token是否已过期
     *
     * @return 是否已过期
     */
    public boolean isExpired() {
        return this.expiresAt != null && LocalDateTime.now().isAfter(this.expiresAt);
    }
} 