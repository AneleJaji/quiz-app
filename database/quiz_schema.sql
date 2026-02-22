-- Quiz System Database Schema
-- Database: quiz_system

-- Create database
CREATE DATABASE IF NOT EXISTS quiz_system;
USE quiz_system;

-- Users table (teachers and students)
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('TEACHER', 'STUDENT') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Quizzes table
CREATE TABLE quizzes (
    quiz_id INT PRIMARY KEY AUTO_INCREMENT,
    quiz_name VARCHAR(100) NOT NULL,
    teacher_id INT NOT NULL,
    total_questions INT NOT NULL,
    time_limit INT, -- seconds per question
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (teacher_id) REFERENCES users(user_id)
);

-- Questions table
CREATE TABLE questions (
    question_id INT PRIMARY KEY AUTO_INCREMENT,
    quiz_id INT NOT NULL,
    question_text TEXT NOT NULL,
    option_a VARCHAR(255) NOT NULL,
    option_b VARCHAR(255) NOT NULL,
    option_c VARCHAR(255) NOT NULL,
    option_d VARCHAR(255) NOT NULL,
    correct_answer CHAR(1) NOT NULL, -- A, B, C, or D
    question_order INT NOT NULL,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(quiz_id) ON DELETE CASCADE
);

-- Student Quiz Attempts table
CREATE TABLE quiz_attempts (
    attempt_id INT PRIMARY KEY AUTO_INCREMENT,
    quiz_id INT NOT NULL,
    student_id INT NOT NULL,
    score INT NOT NULL,
    total_questions INT NOT NULL,
    percentage DECIMAL(5,2),
    time_taken INT, -- in seconds
    attempt_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(quiz_id),
    FOREIGN KEY (student_id) REFERENCES users(user_id)
);

-- Student Answers table (detailed answers per question)
CREATE TABLE student_answers (
    answer_id INT PRIMARY KEY AUTO_INCREMENT,
    attempt_id INT NOT NULL,
    question_id INT NOT NULL,
    selected_answer CHAR(1), -- A, B, C, or D
    is_correct BOOLEAN,
    time_taken INT, -- seconds taken for this question
    FOREIGN KEY (attempt_id) REFERENCES quiz_attempts(attempt_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(question_id)
);

-- Insert default teacher account
INSERT INTO users (username, password, full_name, role) 
VALUES ('teacher', 'teacher123', 'Default Teacher', 'TEACHER');

-- Insert sample students
INSERT INTO users (username, password, full_name, role) 
VALUES 
    ('student1', 'pass123', 'Alice Johnson', 'STUDENT'),
    ('student2', 'pass123', 'Bob Smith', 'STUDENT'),
    ('student3', 'pass123', 'Carol Williams', 'STUDENT');

-- Create indexes for better performance
CREATE INDEX idx_quiz_teacher ON quizzes(teacher_id);
CREATE INDEX idx_quiz_active ON quizzes(is_active);
CREATE INDEX idx_attempt_quiz ON quiz_attempts(quiz_id);
CREATE INDEX idx_attempt_student ON quiz_attempts(student_id);
CREATE INDEX idx_answers_attempt ON student_answers(attempt_id);
