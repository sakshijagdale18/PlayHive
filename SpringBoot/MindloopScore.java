package com.games.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mindloop_scores")
public class MindloopScore {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "BIGINT")
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "BIGINT")
    private User user;
    
    @Column(nullable = false)
    private int score;
    
    @Column(nullable = false)
    private int level;
    
    @Column(name = "time_taken")
    private int timeTaken; // in seconds
    
    @Column(name = "correct_answers")
    private int correctAnswers;
    
    @Column(name = "total_questions")
    private int totalQuestions;
    
    @Column(name = "played_at")  // Changed from playedOn to played_at to match emoji_scores
    private LocalDateTime playedAt;
    
    // Constructors
    public MindloopScore() {
        this.playedAt = LocalDateTime.now();
    }
    
    public MindloopScore(User user, int score, int level, int timeTaken, 
                        int correctAnswers, int totalQuestions) {
        this();
        this.user = user;
        this.score = score;
        this.level = level;
        this.timeTaken = timeTaken;
        this.correctAnswers = correctAnswers;
        this.totalQuestions = totalQuestions;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getTimeTaken() { return timeTaken; }
    public void setTimeTaken(int timeTaken) { this.timeTaken = timeTaken; }

    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public LocalDateTime getPlayedAt() { return playedAt; }
    public void setPlayedAt(LocalDateTime playedAt) { this.playedAt = playedAt; }
}