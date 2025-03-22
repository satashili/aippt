package com.aippt.service;

/**
 * 会员服务接口
 * 处理用户会员资格相关的业务逻辑
 */
public interface MembershipService {
    
    /**
     * 更新用户的会员状态
     * 如果用户已是会员且会员未过期，则在原有期限上延长；否则从当前时间开始计算
     * 
     * @param userId 用户ID
     * @param monthsToAdd 增加的会员月数
     * @return 更新是否成功
     */
    boolean updateMemberStatus(String userId, int monthsToAdd);
    
    /**
     * 检查用户是否是有效会员
     * 
     * @param userId 用户ID
     * @return 是否是有效会员
     */
    boolean isActiveMember(String userId);
} 