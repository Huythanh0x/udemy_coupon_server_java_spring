-- Migration: Add created_at and updated_at timestamps to expired_course_data
-- This migration adds standard timestamp fields to expired_course_data table

-- Step 1: Add created_at column if it doesn't exist
SET @dbname = DATABASE();
SET @tablename = 'expired_course_data';
SET @columnname = 'created_at';
SET @preparedStatement = (SELECT IF(
    (
        SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE
            (table_name = @tablename)
            AND (table_schema = @dbname)
            AND (column_name = @columnname)
    ) > 0,
    'SELECT 1',
    CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' DATETIME NULL')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Step 2: Add updated_at column if it doesn't exist
SET @columnname = 'updated_at';
SET @preparedStatement = (SELECT IF(
    (
        SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE
            (table_name = @tablename)
            AND (table_schema = @dbname)
            AND (column_name = @columnname)
    ) > 0,
    'SELECT 1',
    CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' DATETIME NULL')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Step 3: Populate created_at from existing time_stamp for existing records
UPDATE expired_course_data 
SET created_at = time_stamp 
WHERE created_at IS NULL AND time_stamp IS NOT NULL;

-- Step 4: Set default value for any rows where created_at is still NULL
-- (handles cases where time_stamp was also NULL)
UPDATE expired_course_data 
SET created_at = CURRENT_TIMESTAMP 
WHERE created_at IS NULL;

-- Step 5: Set updated_at to created_at for existing records
UPDATE expired_course_data 
SET updated_at = created_at 
WHERE updated_at IS NULL;

-- Step 6: Make columns NOT NULL with defaults (safe to run even if already NOT NULL)
ALTER TABLE expired_course_data 
MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
MODIFY COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

