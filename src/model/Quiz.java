package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Quiz implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int quizId;
    private String quizName;
    private int teacherId;
    private String teacherName;
    private int totalQuestions;
    private int timeLimit; // seconds per question
    private boolean isActive;
    private List<Question> questions;
    
    public Quiz() {
        this.questions = new ArrayList<>();
    }
    
    public Quiz(String quizName, int teacherId, int timeLimit) {
        this.quizName = quizName;
        this.teacherId = teacherId;
        this.timeLimit = timeLimit;
        this.questions = new ArrayList<>();
        this.isActive = true;
    }
    
    // Getters and Setters
    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }
    
    public String getQuizName() { return quizName; }
    public void setQuizName(String quizName) { this.quizName = quizName; }
    
    public int getTeacherId() { return teacherId; }
    public void setTeacherId(int teacherId) { this.teacherId = teacherId; }
    
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    
    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
    
    public int getTimeLimit() { return timeLimit; }
    public void setTimeLimit(int timeLimit) { this.timeLimit = timeLimit; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { 
        this.questions = questions; 
        this.totalQuestions = questions.size();
    }
    
    public void addQuestion(Question question) {
        this.questions.add(question);
        this.totalQuestions = questions.size();
    }
    
    @Override
    public String toString() {
        return quizName + " (" + totalQuestions + " questions, " + timeLimit + "s each)";
    }
}
