-- 用户表结构
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    id VARCHAR(255) PRIMARY KEY COMMENT '用户ID，手动输入或UUID',
    name VARCHAR(255) NOT NULL COMMENT '用户名',
    email VARCHAR(255) NOT NULL COMMENT '邮箱',
    picture TEXT COMMENT '头像URL',
    password VARCHAR(255) COMMENT '密码（仅本地用户）',
    given_name VARCHAR(255) COMMENT '名',
    family_name VARCHAR(255) COMMENT '姓',
    email_verified BOOLEAN DEFAULT FALSE COMMENT '邮箱是否验证',
    is_member BOOLEAN DEFAULT FALSE COMMENT '是否是会员',
    member_expire_time DATETIME COMMENT '会员到期时间',
    auth_provider VARCHAR(20) DEFAULT 'LOCAL' COMMENT '认证提供者: LOCAL, GOOGLE, BOTH', 
    external_id VARCHAR(255) COMMENT '外部认证ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', 
    last_login DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后登录时间',
    version INT DEFAULT 0 COMMENT '版本号，用于乐观锁',
    deleted INT DEFAULT 0 COMMENT '逻辑删除标志，0表示未删除，1表示已删除',
    
    UNIQUE KEY uk_email (email) COMMENT '确保邮箱唯一'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- 添加索引
CREATE INDEX idx_name ON users (name) COMMENT '用户名索引，用于搜索';
CREATE INDEX idx_auth_provider ON users (auth_provider) COMMENT '认证方式索引，用于过滤查询';
CREATE INDEX idx_external_id ON users (external_id) COMMENT '外部ID索引，用于OAuth2查询';
CREATE INDEX idx_is_member ON users (is_member) COMMENT '会员状态索引';
CREATE INDEX idx_member_expire_time ON users (member_expire_time) COMMENT '会员到期时间索引';