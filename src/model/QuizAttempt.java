package model;

import java.io.Serializable;
import java.sql.Timestamp;

public class QuizAttempt implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int attemptId;
    private int quizId;
    private int studentId;
    private String studentName;
    private String quizName;
    private int score;
    private int totalQuestions;
    private double percentage;
    private int timeTaken; // in seconds
    private Timestamp attemptDate;
    
    public QuizAttempt() {}
    
    public QuizAttempt(int quizId, int studentId, int score, int totalQuestions, int timeTaken) {
        this.quizId = quizId;
        this.studentId = studentId;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.timeTaken = timeTaken;
        this.percentage = (double) score / totalQuestions * 100;
    }
    
    // Getters and Setters
    public int getAttemptId() { return attemptId; }
    public void setAttemptId(int attemptId) { this.attemptId = attemptId; }
    
    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }
    
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    
    public String getQuizName() { return quizName; }
    public void setQuizName(String quizName) { this.quizName = quizName; }
    
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    
    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
    
    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }
    
    public int getTimeTaken() { return timeTaken; }
    public void setTimeTaken(int timeTaken) { this.timeTaken = timeTaken; }
    
    public Timestamp getAttemptDate() { return attemptDate; }
    public void setAttemptDate(Timestamp attemptDate) { this.attemptDate = attemptDate; }
}
