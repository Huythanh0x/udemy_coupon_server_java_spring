-- Create the database if it does not exist
CREATE DATABASE IF NOT EXISTS training_coupon;
USE training_coupon;

-- Define all necessary tables
CREATE TABLE IF NOT EXISTS coupon_course_data
(
    course_id      INT PRIMARY KEY,
    category       VARCHAR(255),
    sub_category   VARCHAR(255),
    title          VARCHAR(255),
    content_length INT,
    level          VARCHAR(255),
    author         VARCHAR(255),
    rating         FLOAT,
    reviews        INT,
    students       INT,
    coupon_code    VARCHAR(255),
    preview_image  VARCHAR(255),
    coupon_url     VARCHAR(255),
    expired_date   VARCHAR(255),
    uses_remaining INT,
    heading        VARCHAR(255),
    description    TEXT,
    preview_video  VARCHAR(255),
    language       VARCHAR(255)
    );

CREATE TABLE IF NOT EXISTS expired_course_data
(
    coupon_url VARCHAR(255) PRIMARY KEY,
    time_stamp TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS roles
(
    id   INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
    );

CREATE TABLE IF NOT EXISTS users
(
    id       INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL
    );

CREATE TABLE IF NOT EXISTS refresh_tokens
(
    id INT PRIMARY KEY AUTO_INCREMENT,
    refresh_token VARCHAR(255) NOT NULL,
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES users(id)
    );

CREATE TABLE IF NOT EXISTS user_roles
(
    user_id INT,
    role_id INT,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (role_id) REFERENCES roles (id)
    );

-- Add roles to roles table
INSERT INTO roles(id, name) VALUES (1, 'USER') ON DUPLICATE KEY UPDATE name=name;
INSERT INTO roles(id, name) VALUES (2, 'ADMIN') ON DUPLICATE KEY UPDATE name=name;