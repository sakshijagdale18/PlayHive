package com.games.service;

import com.games.model.EmojiScore;
import com.games.model.User;
import com.games.repository.EmojiScoreRepository;
import com.games.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EmojiScoreService {
    
    @Autowired
    private EmojiScoreRepository emojiScoreRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Transactional
    public EmojiScore saveScore(Long userId, int score, int level, int timeTaken, 
                               int correctAnswers, int totalQuestions) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        
        User user = userOpt.get();
        
        // Update user game statistics
        user.updateGameStats(score, correctAnswers, timeTaken);
        userRepository.save(user);
        
        // Save individual score record
        EmojiScore emojiScore = new EmojiScore(
            user, score, level, timeTaken, correctAnswers, totalQuestions
        );
        
        return emojiScoreRepository.save(emojiScore);
    }
    
    public List<EmojiScore> getUserScores(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        return emojiScoreRepository.findByUserOrderByScoreDesc(user.get());
    }
    
    public List<EmojiScore> getGlobalLeaderboard() {
        return emojiScoreRepository.findTop10ByOrderByScoreDesc();
    }
    
    // FIXED: Use repository methods that actually exist
    public Optional<Integer> getUserHighScore(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return Optional.empty();
        }
        // Use the repository method that exists
        List<EmojiScore> userScores = emojiScoreRepository.findByUserOrderByScoreDesc(user.get());
        if (userScores.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(userScores.get(0).getScore());
    }
    
    public Optional<Double> getUserAverageScore(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return Optional.empty();
        }
        List<EmojiScore> userScores = emojiScoreRepository.findByUserOrderByScoreDesc(user.get());
        if (userScores.isEmpty()) {
            return Optional.empty();
        }
        double average = userScores.stream()
            .mapToInt(EmojiScore::getScore)
            .average()
            .orElse(0.0);
        return Optional.of(average);
    }
    
    public long getUserGamesPlayed(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return 0L;
        }
        return emojiScoreRepository.countByUser(user.get());
    }
    
    public List<EmojiScore> getUserScoresByLevel(Long userId, int level) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        return emojiScoreRepository.findByUserAndLevelOrderByScoreDesc(user.get(), level);
    }
    
    // This method uses the custom query from repository
    public List<EmojiScore> getFullLeaderboard() {
        return emojiScoreRepository.findLeaderboard();
    }
    
    public boolean deleteScore(Long scoreId) {
        if (emojiScoreRepository.existsById(scoreId)) {
            emojiScoreRepository.deleteById(scoreId);
            return true;
        }
        return false;
    }
}