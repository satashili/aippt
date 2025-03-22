package com.aippt.dto.user;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 会员状态更新请求数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipRequest {
    
    @NotNull(message = "用户ID不能为空")
    private String userId;
    
    @NotNull(message = "月数不能为空")
    @Min(value = 1, message = "月数必须大于0")
    private Integer months;
} 