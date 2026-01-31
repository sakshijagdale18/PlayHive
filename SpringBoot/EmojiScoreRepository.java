package com.games.repository;

import com.games.model.EmojiScore;
import com.games.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmojiScoreRepository extends JpaRepository<EmojiScore, Long> {
    
    // Find scores by user, ordered by score descending
    List<EmojiScore> findByUserOrderByScoreDesc(User user);
    
    // Find top scores globally
    List<EmojiScore> findTop10ByOrderByScoreDesc();
    
    // Find user's highest score
    @Query("SELECT MAX(es.score) FROM EmojiScore es WHERE es.user = :user")
    Optional<Integer> findHighestScoreByUser(@Param("user") User user);
    
    // Find user's average score
    @Query("SELECT AVG(es.score) FROM EmojiScore es WHERE es.user = :user")
    Optional<Double> findAverageScoreByUser(@Param("user") User user);
    
    // Count games played by user
    long countByUser(User user);
    
    // Find scores by level
    List<EmojiScore> findByUserAndLevelOrderByScoreDesc(User user, int level);
    
    // Get leaderboard with user details
    @Query("SELECT es FROM EmojiScore es JOIN FETCH es.user u ORDER BY es.score DESC")
    List<EmojiScore> findLeaderboard();
}