-- 测试数据，仅用于开发环境
-- 注意：密码都是经过BCrypt加密的，原始密码均为"password123"

-- 插入本地注册用户
INSERT INTO users (id, name, email, password, email_verified, is_member, auth_provider, created_at, last_login) 
VALUES 
('local-user-001', '测试用户', 'test@example.com', '$2a$10$Jh4QkIa8QEsFSp5jkKBqj.tVqeGhvGJXZBqLPqzp5x1aR71rXBpLG', true, false, 'LOCAL', NOW(), NOW()),
('local-user-002', '张三', 'zhangsan@example.com', '$2a$10$Jh4QkIa8QEsFSp5jkKBqj.tVqeGhvGJXZBqLPqzp5x1aR71rXBpLG', true, true, 'LOCAL', NOW(), NOW());

-- 插入Google登录用户
INSERT INTO users (id, name, email, picture, given_name, family_name, email_verified, is_member, auth_provider, external_id, created_at, last_login) 
VALUES 
('google-user-001', '李四', 'lisi@gmail.com', 'https://example.com/avatar.jpg', '四', '李', true, false, 'GOOGLE', 'google-123456', NOW(), NOW());

-- 插入双重认证用户（既有本地密码又有Google登录）
INSERT INTO users (id, name, email, password, picture, given_name, family_name, email_verified, is_member, auth_provider, external_id, created_at, last_login) 
VALUES 
('dual-user-001', '王五', 'wangwu@example.com', '$2a$10$Jh4QkIa8QEsFSp5jkKBqj.tVqeGhvGJXZBqLPqzp5x1aR71rXBpLG', 'https://example.com/wangwu.jpg', '五', '王', true, true, 'BOTH', 'google-789012', NOW(), NOW());

-- 设置会员过期时间
UPDATE users SET member_expire_time = DATE_ADD(NOW(), INTERVAL 30 DAY) WHERE id = 'local-user-002';
UPDATE users SET member_expire_time = DATE_ADD(NOW(), INTERVAL 60 DAY) WHERE id = 'dual-user-001'; 