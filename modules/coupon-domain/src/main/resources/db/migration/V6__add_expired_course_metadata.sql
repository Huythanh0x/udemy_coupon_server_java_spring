-- Migration: Add courseId and title to expired_course_data
-- This migration adds courseId and title fields to store more metadata about expired coupons
-- This allows us to reuse courseId without making HTTP requests when rechecking expired coupons

-- Step 1: Add course_id column if it doesn't exist
SET @dbname = DATABASE();
SET @tablename = 'expired_course_data';
SET @columnname = 'course_id';
SET @preparedStatement = (SELECT IF(
    (
        SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE
            (table_name = @tablename)
            AND (table_schema = @dbname)
            AND (column_name = @columnname)
    ) > 0,
    'SELECT 1',
    CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' INT NULL')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Step 2: Add title column if it doesn't exist
SET @columnname = 'title';
SET @preparedStatement = (SELECT IF(
    (
        SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE
            (table_name = @tablename)
            AND (table_schema = @dbname)
            AND (column_name = @columnname)
    ) > 0,
    'SELECT 1',
    CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' VARCHAR(500) NULL')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Step 3: Populate courseId and title from coupon_course_data for existing expired entries
-- This backfills data for expired coupons that are still in the main table
UPDATE expired_course_data ecd
INNER JOIN coupon_course_data ccd ON ecd.coupon_url = ccd.coupon_url
SET 
    ecd.course_id = ccd.course_id,
    ecd.title = ccd.title
WHERE ecd.course_id IS NULL;

