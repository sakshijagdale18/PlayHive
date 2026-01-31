package com.games.controller;

import com.games.model.MindloopScore;
import com.games.model.User;
import com.games.service.MindloopScoreService;
import com.games.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mindloop")
@CrossOrigin(
	    origins = { "http://localhost:5500", "http://127.0.0.1:5500" },
	    allowCredentials = "true"
	)
public class MindloopController {

    @Autowired
    private MindloopScoreService mindloopScoreService;

    @Autowired
    private UserRepository userRepository;

    // Request DTO for score submission by email
    public static class EmailScoreRequest {
        private String email;
        private int score;
        private int level;
        private int timeTaken;
        private int correctAnswers;
        private int totalQuestions;

        // Getters and Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
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
    }

    // 🟢 SUBMIT SCORE BY EMAIL - SIMPLE VERSION
   // 🟢 SUBMIT SCORE BY EMAIL - SIMPLE VERSION
@PostMapping("/submit-score-by-email-simple")
public ResponseEntity<?> submitScoreByEmailSimple(@RequestBody EmailScoreRequest scoreRequest) {
    
    System.out.println("🎯 MINDLOOP SIMPLE VERSION: Received score submission by email:");
    System.out.println("   Email: " + scoreRequest.getEmail());
    System.out.println("   Score: " + scoreRequest.getScore());
    
    try {
        // Validate request
        if (scoreRequest.getEmail() == null || scoreRequest.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Email is required"));
        }
        
        String email = scoreRequest.getEmail().trim().toLowerCase();
        
        // Find user by email or create new one if doesn't exist
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;
        
        if (userOptional.isEmpty()) {
            // Create new user
            System.out.println("👤 Creating new user for email: " + email);
            user = new User();
            user.setEmail(email);
            user.setUsername(email.split("@")[0]); // Use part before @ as username            
            // Initialize all fields
            user.setLevel(1);
            user.setScore(0);
            user.setGamesPlayed(0);
            user.setHighestScore(0);
            user.setAverageScore(0.0);
            user.setTotalCorrectAnswers(0);
            user.setTotalTimePlayed(0);
            user.setAccuracy(0.0);
            
            user = userRepository.save(user);
            System.out.println("✅ New user created with ID: " + user.getId());
        } else {
            user = userOptional.get();
            System.out.println("👤 Existing user found: " + user.getUsername() + " (ID: " + user.getId() + ")");
        }
        
        // Initialize user fields if needed (safety check)
        initializeUserFields(user);
        
        // Save the score
        MindloopScore savedScore = mindloopScoreService.saveScore(
            user.getId(), 
            scoreRequest.getScore(), 
            scoreRequest.getLevel(), 
            scoreRequest.getTimeTaken(), 
            scoreRequest.getCorrectAnswers(), 
            scoreRequest.getTotalQuestions()
        );
        
        System.out.println("✅ Mindloop score saved successfully with ID: " + savedScore.getId());
        System.out.println("   User: " + user.getUsername() + " (ID: " + user.getId() + ")");
        System.out.println("   Score: " + scoreRequest.getScore() + ", Level: " + scoreRequest.getLevel());
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("scoreId", savedScore.getId());
        response.put("message", "Mindloop score saved successfully");
        response.put("username", user.getUsername());
        response.put("userId", user.getId());
        
        return ResponseEntity.ok(response);
        
    } catch (Exception e) {
        System.out.println("❌ Error saving mindloop score: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error saving mindloop score: " + e.getMessage()));
    }
}

    // 🟢 GET USER SCORES BY EMAIL
    @GetMapping("/scores-by-email")
    public ResponseEntity<?> getUserScoresByEmail(@RequestParam String email) {
        try {
            // Find user by email
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("User not found with email: " + email));
            }
            
            User user = userOptional.get();
            List<MindloopScore> scores = mindloopScoreService.getUserScores(user.getId());
            
            // Convert to response DTO
            List<Map<String, Object>> scoreResponses = scores.stream()
                .map(this::convertToScoreResponse)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(scoreResponses);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error retrieving mindloop scores: " + e.getMessage()));
        }
    }

    // 🟢 GET GLOBAL LEADERBOARD
    @GetMapping("/leaderboard")
    public ResponseEntity<?> getGlobalLeaderboard() {
        try {
            List<MindloopScore> leaderboard = mindloopScoreService.getGlobalLeaderboard();
            List<Map<String, Object>> leaderboardResponse = leaderboard.stream()
                .map(this::convertToLeaderboardResponse)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(leaderboardResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error retrieving leaderboard: " + e.getMessage()));
        }
    }

    // 🟢 GET USER STATISTICS
    @GetMapping("/user-stats")
    public ResponseEntity<?> getUserStats(@RequestParam String email) {
        try {
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("User not found with email: " + email));
            }
            
            User user = userOptional.get();
            Long userId = user.getId();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("username", user.getUsername());
            stats.put("totalGamesPlayed", mindloopScoreService.getUserGamesPlayed(userId));
            stats.put("highScore", mindloopScoreService.getUserHighScore(userId).orElse(0));
            stats.put("averageScore", mindloopScoreService.getUserAverageScore(userId).orElse(0.0));
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error retrieving user stats: " + e.getMessage()));
        }
    }

    // Helper method to initialize user fields
    private void initializeUserFields(User user) {
        if (user.getLevel() == null) user.setLevel(1);
        if (user.getScore() == null) user.setScore(0);
        if (user.getGamesPlayed() == null) user.setGamesPlayed(0);
        if (user.getHighestScore() == null) user.setHighestScore(0);
        if (user.getAverageScore() == null) user.setAverageScore(0.0);
        if (user.getTotalCorrectAnswers() == null) user.setTotalCorrectAnswers(0);
        if (user.getTotalTimePlayed() == null) user.setTotalTimePlayed(0);
        if (user.getAccuracy() == null) user.setAccuracy(0.0);
        if (user.getAverageTimePerGame() == null) user.setAverageTimePerGame(0.0);
        
        // Save the initialized user
        userRepository.save(user);
    }

    // Helper method to convert score to response
    private Map<String, Object> convertToScoreResponse(MindloopScore score) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", score.getId());
        response.put("score", score.getScore());
        response.put("level", score.getLevel());
        response.put("timeTaken", score.getTimeTaken());
        response.put("correctAnswers", score.getCorrectAnswers());
        response.put("totalQuestions", score.getTotalQuestions());
        response.put("username", score.getUser().getUsername());
        return response;
    }

    // Helper method for leaderboard response
    private Map<String, Object> convertToLeaderboardResponse(MindloopScore score) {
        Map<String, Object> response = new HashMap<>();
        response.put("username", score.getUser().getUsername());
        response.put("score", score.getScore());
        response.put("level", score.getLevel());
        response.put("timeTaken", score.getTimeTaken());
        response.put("playedOn", score.getPlayedAt()); 
        return response;
    }

    // Helper method to create error response
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", message);
        return errorResponse;
    }
}