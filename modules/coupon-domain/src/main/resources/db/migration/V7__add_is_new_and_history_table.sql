-- Migration: Add is_new column and coupon_course_history table
-- This migration adds an is_new flag to coupon_course_data and creates a history table

SET @dbname = DATABASE();

-- Step 1: Add is_new column to coupon_course_data if it doesn't exist
SET @tablename = 'coupon_course_data';
SET @columnname = 'is_new';
SET @preparedStatement = (SELECT IF(
    (
        SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE
            (table_name = @tablename)
            AND (table_schema = @dbname)
            AND (column_name = @columnname)
    ) > 0,
    'SELECT 1',
    CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' TINYINT(1) NOT NULL DEFAULT 1')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Step 2: Create coupon_course_history table if it doesn't exist
CREATE TABLE IF NOT EXISTS coupon_course_history
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id INT NULL,
    title VARCHAR(500) NULL,
    coupon_url VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_coupon_course_history_coupon_url (coupon_url),
    INDEX idx_coupon_course_history_status (status)
);

