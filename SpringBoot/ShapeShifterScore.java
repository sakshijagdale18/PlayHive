package com.games.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "shape_shifter_scores")
public class ShapeShifterScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private int level;
    private int score;
    private int streak;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public ShapeShifterScore() {}

    public ShapeShifterScore(String username, int level, int score, int streak) {
        this.username = username;
        this.level = level;
        this.score = score;
        this.streak = streak;
    }

    // Getters and Setters
    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public int getLevel() { return level; }

    public void setLevel(int level) { this.level = level; }

    public int getScore() { return score; }

    public void setScore(int score) { this.score = score; }

    public int getStreak() { return streak; }

    public void setStreak(int streak) { this.streak = streak; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}