#database if not exist
create database if not exists training_coupon;
use training_coupon;
#drop old table
drop table if exists expired_course_data;
drop table if exists coupon_course_data;
drop table if exists log_app_data;
drop table if exists users_roles;
drop table if exists users;
drop table if exists roles;
#define all necessary tables
CREATE TABLE coupon_course_data
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

CREATE TABLE expired_course_data
(
    coupon_url VARCHAR(255) PRIMARY KEY,
    time_stamp TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE roles
(
    id   INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE users
(
    id       INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE user_roles
(
    user_id INT,
    role_id INT,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (role_id) REFERENCES roles (id)
);

#add role to roles table
INSERT INTO roles(id, name)
VALUES (1, "USER");

INSERT INTO roles(id, name)
VALUES (2, "ADMIN");