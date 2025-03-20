CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    picture TEXT,
    password VARCHAR(255),
    given_name VARCHAR(255),
    family_name VARCHAR(255),
    email_verified BOOLEAN,
    is_member BOOLEAN DEFAULT FALSE,
    member_expire_time DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时自动填充
    last_login DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 更新时自动填充
    version INT DEFAULT 0,
    deleted INT DEFAULT 0,
    UNIQUE KEY uk_email (email) -- 确保 email 唯一
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 可选：为 name 字段添加索引
CREATE INDEX idx_name ON users (name);