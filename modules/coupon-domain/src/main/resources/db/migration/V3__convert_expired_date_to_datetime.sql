-- Migration: Convert expired_date from VARCHAR to DATETIME
-- This migration converts the expired_date column from VARCHAR(255) to DATETIME
-- to enable better date handling, queries, and type safety.

-- Step 1: Add a temporary column for the converted date
ALTER TABLE coupon_course_data 
ADD COLUMN expired_date_temp DATETIME NULL;

-- Step 2: Convert valid date strings to DATETIME
UPDATE coupon_course_data 
SET expired_date_temp = STR_TO_DATE(SUBSTRING(expired_date, 1, 19), '%Y-%m-%d %H:%i:%s')
WHERE expired_date IS NOT NULL 
  AND expired_date != ''
  AND LENGTH(expired_date) >= 19
  AND STR_TO_DATE(SUBSTRING(expired_date, 1, 19), '%Y-%m-%d %H:%i:%s') IS NOT NULL;

-- Step 3: Set default far-future date for invalid/missing dates
UPDATE coupon_course_data 
SET expired_date_temp = '2099-12-31 23:59:59'
WHERE expired_date_temp IS NULL;

-- Step 4: Drop the old VARCHAR column
ALTER TABLE coupon_course_data 
DROP COLUMN expired_date;

-- Step 5: Rename the temp column to expired_date
ALTER TABLE coupon_course_data 
CHANGE COLUMN expired_date_temp expired_date DATETIME NOT NULL DEFAULT '2099-12-31 23:59:59';

-- Step 6: Add index for better query performance (for Proposal 2)
CREATE INDEX idx_expired_date ON coupon_course_data(expired_date);

