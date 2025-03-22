-- 验证令牌表创建语句
CREATE TABLE IF NOT EXISTS verification_token (
    id VARCHAR(255) PRIMARY KEY COMMENT '主键ID',
    token VARCHAR(255) NOT NULL COMMENT '验证令牌',
    user_id VARCHAR(255) NOT NULL COMMENT '关联的用户ID',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    expires_at DATETIME NOT NULL COMMENT '过期时间',
    confirmed_at DATETIME NULL COMMENT '确认时间',

    INDEX idx_token (token) COMMENT '令牌索引，用于快速查找',
    INDEX idx_user_id (user_id) COMMENT '用户ID索引，用于关联查询'
    -- 移除外键约束解决不兼容问题
    -- CONSTRAINT fk_verification_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='邮箱验证令牌表';