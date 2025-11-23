-- Migration: Create scraped_url_mapping table
-- This migration creates a table to map scraped URLs to their corresponding Udemy coupon URLs.
CREATE TABLE IF NOT EXISTS scraped_url_mapping (
    scraped_url VARCHAR(500) PRIMARY KEY,
    coupon_url VARCHAR(255) NOT NULL,
    crawler_source VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_coupon_url (coupon_url),
    INDEX idx_crawler_source (crawler_source)
);

