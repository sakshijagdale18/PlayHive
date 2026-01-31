package com.games.service;

import com.games.model.MindloopScore;
import com.games.model.User;
import com.games.repository.MindloopScoreRepository;
import com.games.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MindloopScoreService {
    
    @Autowired
    private MindloopScoreRepository mindloopScoreRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Transactional
    public MindloopScore saveScore(Long userId, int score, int level, int timeTaken, 
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
        MindloopScore mindloopScore = new MindloopScore(
            user, score, level, timeTaken, correctAnswers, totalQuestions
        );
        
        return mindloopScoreRepository.save(mindloopScore);
    }
    
    public List<MindloopScore> getUserScores(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        return mindloopScoreRepository.findByUserOrderByScoreDesc(user.get());
    }
    
    public List<MindloopScore> getGlobalLeaderboard() {
        return mindloopScoreRepository.findTop10ByOrderByScoreDesc();
    }
    
    public Optional<Integer> getUserHighScore(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return Optional.empty();
        }
        List<MindloopScore> userScores = mindloopScoreRepository.findByUserOrderByScoreDesc(user.get());
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
        List<MindloopScore> userScores = mindloopScoreRepository.findByUserOrderByScoreDesc(user.get());
        if (userScores.isEmpty()) {
            return Optional.empty();
        }
        double average = userScores.stream()
            .mapToInt(MindloopScore::getScore)
            .average()
            .orElse(0.0);
        return Optional.of(average);
    }
    
    public long getUserGamesPlayed(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return 0L;
        }
        return mindloopScoreRepository.countByUser(user.get());
    }
    
    public List<MindloopScore> getUserScoresByLevel(Long userId, int level) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        return mindloopScoreRepository.findByUserAndLevelOrderByScoreDesc(user.get(), level);
    }
    
    public List<MindloopScore> getFullLeaderboard() {
        return mindloopScoreRepository.findLeaderboard();
    }
    
    public boolean deleteScore(Long scoreId) {
        if (mindloopScoreRepository.existsById(scoreId)) {
            mindloopScoreRepository.deleteById(scoreId);
            return true;
        }
        return false;
    }
}