package com.games.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "BIGINT")
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "registration_date", nullable = false)
    private LocalDateTime registrationDate = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; 

    @Column(name = "original_username")
    private String originalUsername;

    @Column(name = "original_email")
    private String originalEmail;

    @Column(name = "original_password")
    private String originalPassword;

    // Game-related fields
    private Integer score = 0;
    private Integer level = 1;
    
    @Column(name = "average_time_per_game")
    private Double averageTimePerGame = 0.0;
    
    
    // New fields for EmojiDecoder game
    @Column(name = "games_played")
    private Integer gamesPlayed = 0;
    
    @Column(name = "total_correct_answers")
    private Integer totalCorrectAnswers = 0;
    
    @Column(name = "total_time_played")
    private Integer totalTimePlayed = 0;
    
    @Column(name = "highest_score")
    private Integer highestScore = 0;
    
    @Column(name = "average_score")
    private Double averageScore = 0.0;
    
    @Column(name = "last_played")
    private LocalDateTime lastPlayed;
    
    @Column(name = "accuracy")
    private Double accuracy = 0.0;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    // Relationship with EmojiScore (One-to-Many)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EmojiScore> emojiScores = new ArrayList<>();

    public enum Status {
        ACTIVE,
        BANNED,
        DEACTIVATED
    }

    public User() {}

    @PrePersist
    public void onCreate() {
        this.registrationDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.originalUsername = this.username;
        this.originalEmail = this.email;
        this.originalPassword = this.password;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDateTime registrationDate) { this.registrationDate = registrationDate; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getOriginalUsername() { return originalUsername; }
    public void setOriginalUsername(String originalUsername) { this.originalUsername = originalUsername; }

    public String getOriginalEmail() { return originalEmail; }
    public void setOriginalEmail(String originalEmail) { this.originalEmail = originalEmail; }

    public String getOriginalPassword() { return originalPassword; }
    public void setOriginalPassword(String originalPassword) { this.originalPassword = originalPassword; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public Integer getGamesPlayed() { return gamesPlayed; }
    public void setGamesPlayed(Integer gamesPlayed) { this.gamesPlayed = gamesPlayed; }

    public Integer getTotalCorrectAnswers() { return totalCorrectAnswers; }
    public void setTotalCorrectAnswers(Integer totalCorrectAnswers) { this.totalCorrectAnswers = totalCorrectAnswers; }

    public Integer getTotalTimePlayed() { return totalTimePlayed; }
    public void setTotalTimePlayed(Integer totalTimePlayed) { this.totalTimePlayed = totalTimePlayed; }

    public Integer getHighestScore() { return highestScore; }
    public void setHighestScore(Integer highestScore) { this.highestScore = highestScore; }

    public Double getAverageScore() { return averageScore; }
    public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }

    public LocalDateTime getLastPlayed() { return lastPlayed; }
    public void setLastPlayed(LocalDateTime lastPlayed) { this.lastPlayed = lastPlayed; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public List<EmojiScore> getEmojiScores() { return emojiScores; }
    public void setEmojiScores(List<EmojiScore> emojiScores) { this.emojiScores = emojiScores; }

    // Utility methods
    public Double getAccuracy() { 
        return accuracy != null ? accuracy : 0.0; 
    }
    
    
    public void setAccuracy(Double accuracy) { 
        this.accuracy = accuracy; 
    }

    // ✅ UPDATE the updateGameStats method to also update accuracy
    public void updateGameStats(int newScore, int correctAnswers, int timeTaken) {
        this.gamesPlayed++;
        this.totalCorrectAnswers += correctAnswers;
        this.totalTimePlayed += timeTaken;
        this.score += newScore;
        
        if (newScore > this.highestScore) {
            this.highestScore = newScore;
        }
        
        this.averageScore = (this.averageScore * (gamesPlayed - 1) + newScore) / gamesPlayed;
        
        // ✅ Update accuracy based on correct answers
        this.accuracy = gamesPlayed > 0 ? (double) totalCorrectAnswers / (gamesPlayed * 10) * 100 : 0.0;
        
        this.lastPlayed = LocalDateTime.now();
        
        // Auto-level up logic
        if (this.score >= 5000 && this.level < 2) {
            this.level = 2;
        } else if (this.score >= 15000 && this.level < 3) {
            this.level = 3;
        } else if (this.score >= 30000 && this.level < 4) {
            this.level = 4;
        } else if (this.score >= 50000 && this.level < 5) {
            this.level = 5;
        }
    }

    
    public Double getAverageTimePerGame() {
        return averageTimePerGame != null ? averageTimePerGame : 0.0;
    }
    
    public void setAverageTimePerGame(Double averageTimePerGame) {
        this.averageTimePerGame = averageTimePerGame != null ? averageTimePerGame : 0.0;
    }
}