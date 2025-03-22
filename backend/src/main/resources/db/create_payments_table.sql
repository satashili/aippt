-- 创建支付记录表
CREATE TABLE IF NOT EXISTS payments (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36),
    payment_intent_id VARCHAR(100) NOT NULL,
    plan_type VARCHAR(20) NOT NULL,
    amount BIGINT NOT NULL,
    currency VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    cancelled BOOLEAN DEFAULT FALSE,
    cancelled_at TIMESTAMP,
    metadata TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create indexes
CREATE INDEX idx_payments_user_id ON payments(user_id);
CREATE INDEX idx_payments_payment_intent_id ON payments(payment_intent_id);
CREATE INDEX idx_payments_status ON payments(status); 