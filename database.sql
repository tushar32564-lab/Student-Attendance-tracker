-- ============================================================
-- Student Attendance Tracker - MySQL Database Script
-- Run this file once in MySQL Workbench or on the mysql client.
-- ============================================================

-- Recreate the database (remove the DROP line to keep existing data)
DROP DATABASE IF EXISTS attendance_db;
CREATE DATABASE attendance_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE attendance_db;

-- ------------------------------------------------------------
-- Table: users  (login accounts)
-- ------------------------------------------------------------
CREATE TABLE users (
    user_id   INT PRIMARY KEY AUTO_INCREMENT,
    username  VARCHAR(50) UNIQUE NOT NULL,
    password  VARCHAR(100) NOT NULL
) ENGINE = InnoDB;

-- ------------------------------------------------------------
-- Table: students
-- ------------------------------------------------------------
CREATE TABLE students (
    student_id INT PRIMARY KEY AUTO_INCREMENT,
    roll_no    VARCHAR(20) UNIQUE NOT NULL,
    name       VARCHAR(100) NOT NULL,
    course     VARCHAR(50) NOT NULL,
    semester   INT NOT NULL
) ENGINE = InnoDB;

-- ------------------------------------------------------------
-- Table: attendance
-- ------------------------------------------------------------
CREATE TABLE attendance (
    attendance_id   INT PRIMARY KEY AUTO_INCREMENT,
    student_id      INT NOT NULL,
    attendance_date DATE NOT NULL,
    status          VARCHAR(10) NOT NULL,
    CONSTRAINT fk_attendance_student
        FOREIGN KEY (student_id) REFERENCES students (student_id)
) ENGINE = InnoDB;

-- Speeds up "attendance already marked on this date" checks
CREATE INDEX idx_attendance_date ON attendance (attendance_date);

-- ------------------------------------------------------------
-- Default login user
-- username: admin   password: admin123
-- ------------------------------------------------------------
INSERT INTO users (username, password)
VALUES ('admin', 'admin123');

-- ------------------------------------------------------------
-- OPTIONAL: one-time setup of the application database account.
-- Uncomment, change YOUR_PASSWORD, run once, then keep the same
-- password in util/DatabaseConnection.java (or set DB_PASSWORD).
-- ------------------------------------------------------------
-- CREATE USER IF NOT EXISTS 'attendance_user'@'localhost'
--     IDENTIFIED BY 'YOUR_PASSWORD';
-- GRANT ALL PRIVILEGES ON attendance_db.* TO 'attendance_user'@'localhost';
-- FLUSH PRIVILEGES;
