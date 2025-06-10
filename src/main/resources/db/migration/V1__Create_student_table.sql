-- V1__Create_student_table.sql
-- Create student table and sequence

CREATE SEQUENCE IF NOT EXISTS student_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE student (
                         id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         email VARCHAR(255) NOT NULL UNIQUE,
                         dob DATE NOT NULL
);

-- Create index on email for better query performance
CREATE INDEX idx_student_email ON student(email);