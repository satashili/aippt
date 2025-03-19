CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    picture TEXT,
    given_name VARCHAR(255),
    family_name VARCHAR(255),
    email_verified BOOLEAN,
    is_member BOOLEAN DEFAULT FALSE,
    member_expire_time DATETIME,
    created_at DATETIME NOT NULL,
    last_login DATETIME NOT NULL,
    version INT DEFAULT 0,
    deleted INT DEFAULT 0,
    UNIQUE KEY uk_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4; 