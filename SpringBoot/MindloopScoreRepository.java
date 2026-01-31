package com.games.repository;

import com.games.model.MindloopScore;
import com.games.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MindloopScoreRepository extends JpaRepository<MindloopScore, Long> {
    
    // Find all scores for a user, ordered by score descending
    List<MindloopScore> findByUserOrderByScoreDesc(User user);
    
    // Find top 10 global scores
    List<MindloopScore> findTop10ByOrderByScoreDesc();
    
    // Count games played by user
    long countByUser(User user);
    
    // Find scores by user and level
    List<MindloopScore> findByUserAndLevelOrderByScoreDesc(User user, int level);
    
    // Custom query for full leaderboard - updated to use playedAt
    @Query("SELECT ms FROM MindloopScore ms ORDER BY ms.score DESC, ms.timeTaken ASC")
    List<MindloopScore> findLeaderboard();
}