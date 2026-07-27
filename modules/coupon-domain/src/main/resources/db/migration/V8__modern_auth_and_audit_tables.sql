-- Migration: Add modern auth and audit tables
-- This migration syncs the database with UserEntity, SocialAccount, PasskeyCredential, and ScrapingTaskLog

-- 1. Update users table
ALTER TABLE users
ADD COLUMN full_name VARCHAR(255) NULL,
ADD COLUMN email VARCHAR(255) NULL UNIQUE,
ADD COLUMN fcm_token VARCHAR(500) NULL;

-- 2. Create user_social_accounts table
CREATE TABLE IF NOT EXISTS user_social_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    provider VARCHAR(32) NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    user_id INT NOT NULL,
    CONSTRAINT fk_social_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_provider_id (provider, provider_id)
);

-- 3. Create user_passkey_credentials table
CREATE TABLE IF NOT EXISTS user_passkey_credentials (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    credential_id VARBINARY(1024) NOT NULL,
    public_key VARBINARY(2048) NOT NULL,
    signature_count BIGINT DEFAULT 0,
    user_id INT NOT NULL,
    CONSTRAINT fk_passkey_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_credential_id (credential_id)
);

-- 4. Create user_preferences table
CREATE TABLE IF NOT EXISTS user_preferences (
    user_id INT PRIMARY KEY,
    notifications_enabled TINYINT(1) DEFAULT 1,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_preference_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 5. Create preference collection tables
CREATE TABLE IF NOT EXISTS user_preference_categories (
    user_id INT NOT NULL,
    category VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, category),
    CONSTRAINT fk_pref_cat_user FOREIGN KEY (user_id) REFERENCES user_preferences(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_preference_keywords (
    user_id INT NOT NULL,
    keyword VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, keyword),
    CONSTRAINT fk_pref_key_user FOREIGN KEY (user_id) REFERENCES user_preferences(user_id) ON DELETE CASCADE
);

-- 6. Create scraping_task_logs table
CREATE TABLE IF NOT EXISTS scraping_task_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    url VARCHAR(2048) NOT NULL,
    status VARCHAR(32) NOT NULL, -- PENDING, SUCCESS, FAILED
    error_message TEXT,
    remote_addr VARCHAR(64),
    course_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
