package com.games.controller;

import com.games.model.EmojiScore;
import com.games.model.User;
import com.games.repository.UserRepository;
import com.games.service.EmojiScoreService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/emoji")
@CrossOrigin(
    origins = { "http://localhost:5500", "http://127.0.0.1:5500" },
    allowCredentials = "true"
)
public class EmojiScoreController {
    
    @Autowired
    private EmojiScoreService emojiScoreService;
    
    @Autowired
    private UserRepository userRepository;
    
    // 🟢 SUBMIT SCORE - FIXED VERSION (uses JSON body instead of @RequestParam)
    @PostMapping("/submit-score")
    public ResponseEntity<?> submitScore(@RequestBody ScoreRequest scoreRequest, HttpSession session) {
        
        System.out.println("🎯 Received score submission:");
        System.out.println("   Score: " + scoreRequest.getScore());
        System.out.println("   Level: " + scoreRequest.getLevel());
        System.out.println("   Time: " + scoreRequest.getTimeTaken() + "s");
        System.out.println("   Correct: " + scoreRequest.getCorrectAnswers() + "/" + scoreRequest.getTotalQuestions());
        
        // Check if user is logged in
        Object userObj = session.getAttribute("loggedInUser");
        if (userObj == null) {
            System.out.println("❌ User not logged in session");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("User not logged in"));
        }
        
        try {
            User user = (User) userObj;
            System.out.println("👤 Saving score for user: " + user.getUsername() + " (ID: " + user.getId() + ")");
            
            EmojiScore savedScore = emojiScoreService.saveScore(
                user.getId(), 
                scoreRequest.getScore(), 
                scoreRequest.getLevel(), 
                scoreRequest.getTimeTaken(), 
                scoreRequest.getCorrectAnswers(), 
                scoreRequest.getTotalQuestions()
            );
            
            System.out.println("✅ Score saved successfully with ID: " + savedScore.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("scoreId", savedScore.getId());
            response.put("message", "Score saved successfully");
            response.put("updatedStats", getUserGameStats(user.getId()));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("❌ Error saving score: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error saving score: " + e.getMessage()));
        }
    }
    
    // 🔵 GET USER SCORES - FIXED
    @GetMapping("/my-scores")
    public ResponseEntity<?> getUserScores(HttpSession session) {
        Object userObj = session.getAttribute("loggedInUser");
        if (userObj == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("User not logged in"));
        }
        
        try {
            User user = (User) userObj; // ✅ Fixed package reference
            List<EmojiScore> scores = emojiScoreService.getUserScores(user.getId());
            
            // Convert to response DTO to avoid lazy loading issues
            List<Map<String, Object>> scoreResponses = scores.stream()
                .map(this::convertToScoreResponse)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(scoreResponses);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error retrieving scores: " + e.getMessage()));
        }
    }
    
    // 🟡 GET LEADERBOARD - FIXED
    @GetMapping("/leaderboard")
public ResponseEntity<?> getLeaderboard() {
    try {
        List<EmojiScore> leaderboard = emojiScoreService.getGlobalLeaderboard();
        
        // Convert to response DTO with email
        List<Map<String, Object>> leaderboardResponse = leaderboard.stream()
            .map(score -> {
                Map<String, Object> response = new HashMap<>();
                response.put("id", score.getId());
                response.put("score", score.getScore());
                response.put("level", score.getLevel());
                response.put("timeTaken", score.getTimeTaken());
                response.put("correctAnswers", score.getCorrectAnswers());
                response.put("totalQuestions", score.getTotalQuestions());
                response.put("playedAt", score.getPlayedAt());
                response.put("username", score.getUser().getUsername());
                response.put("userEmail", score.getUser().getEmail()); // Add email for highlighting
                return response;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(leaderboardResponse);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error retrieving leaderboard: " + e.getMessage()));
    }
}
    
    // 🟣 GET USER STATS - FIXED
    @GetMapping("/my-stats")
    public ResponseEntity<?> getUserStats(HttpSession session) {
        Object userObj = session.getAttribute("loggedInUser");
        if (userObj == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("User not logged in"));
        }
        
        try {
            User user = (User) userObj; // ✅ Fixed package reference
            Long userId = user.getId();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("gamesPlayed", emojiScoreService.getUserGamesPlayed(userId));
            
            Optional<Integer> highScore = emojiScoreService.getUserHighScore(userId);
            stats.put("highScore", highScore.orElse(0));
            
            Optional<Double> avgScore = emojiScoreService.getUserAverageScore(userId);
            stats.put("averageScore", avgScore.orElse(0.0));
            
            // Add more stats
            stats.put("totalScores", emojiScoreService.getUserScores(userId).size());
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error retrieving stats: " + e.getMessage()));
        }
    }
    
    // 🔴 DELETE SCORE - FIXED
    @DeleteMapping("/score/{scoreId}")
    public ResponseEntity<?> deleteScore(@PathVariable Long scoreId, HttpSession session) {
        Object userObj = session.getAttribute("loggedInUser");
        if (userObj == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("User not logged in"));
        }
        
        try {
            boolean deleted = emojiScoreService.deleteScore(scoreId);
            if (deleted) {
                return ResponseEntity.ok(createSuccessResponse("Score deleted successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Score not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error deleting score: " + e.getMessage()));
        }
    }
    
    // 🟠 GET LEVEL SCORES - FIXED
    @GetMapping("/level/{level}")
    public ResponseEntity<?> getLevelScores(@PathVariable int level, HttpSession session) {
        Object userObj = session.getAttribute("loggedInUser");
        if (userObj == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("User not logged in"));
        }
        
        try {
            User user = (User) userObj; // ✅ Fixed package reference
            List<EmojiScore> scores = emojiScoreService.getUserScoresByLevel(user.getId(), level);
            
            // Convert to response DTO
            List<Map<String, Object>> scoreResponses = scores.stream()
                .map(this::convertToScoreResponse)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(scoreResponses);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error retrieving level scores: " + e.getMessage()));
        }
    }
    
    // 🟢 DEBUG ENDPOINT
    @GetMapping("/debug")
    public ResponseEntity<?> debug() {
        Map<String, Object> debugInfo = new HashMap<>();
        debugInfo.put("status", "EmojiScoreController is running");
        debugInfo.put("timestamp", LocalDateTime.now());
        debugInfo.put("endpoints", List.of(
            "POST /api/emoji/submit-score",
            "GET /api/emoji/leaderboard", 
            "GET /api/emoji/my-scores",
            "GET /api/emoji/my-stats",
            "GET /api/emoji/game-stats",
            "GET /api/emoji/debug"
        ));
        return ResponseEntity.ok(debugInfo);
    }
    
    // 🎮 GET USER GAME STATS - FIXED (moved from UserController)
    @GetMapping("/game-stats")
    public ResponseEntity<?> getUserGameStats(HttpSession session) {
        Object userObj = session.getAttribute("loggedInUser");
        if (userObj == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("User not logged in"));
        }
        
        try {
            User sessionUser = (User) userObj;
            // Refresh user data from database
            Optional<User> currentUser = userRepository.findById(sessionUser.getId());
            if (currentUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("User not found"));
            }
            
            User freshUser = currentUser.get();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("username", freshUser.getUsername());
            stats.put("level", freshUser.getLevel());
            stats.put("totalScore", freshUser.getScore());
            stats.put("highestScore", freshUser.getHighestScore());
            stats.put("averageScore", freshUser.getAverageScore());
            stats.put("gamesPlayed", freshUser.getGamesPlayed());
            stats.put("totalCorrectAnswers", freshUser.getTotalCorrectAnswers());
            stats.put("totalTimePlayed", freshUser.getTotalTimePlayed());
            stats.put("accuracy", freshUser.getAccuracy());
            stats.put("averageTimePerGame", freshUser.getAverageTimePerGame());
            stats.put("lastPlayed", freshUser.getLastPlayed());
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error retrieving game stats: " + e.getMessage()));
        }
    }
    
    // 🟢 CHECK SESSION STATUS
    @GetMapping("/session-status")
    public ResponseEntity<?> checkSessionStatus(HttpSession session) {
        Map<String, Object> sessionInfo = new HashMap<>();
        sessionInfo.put("sessionId", session.getId());
        sessionInfo.put("isNew", session.isNew());
        sessionInfo.put("creationTime", new Date(session.getCreationTime()));
        sessionInfo.put("lastAccessedTime", new Date(session.getLastAccessedTime()));
        
        Object userObj = session.getAttribute("loggedInUser");
        if (userObj != null) {
            User user = (User) userObj;
            sessionInfo.put("loggedInUser", user.getUsername());
            sessionInfo.put("userId", user.getId());
            sessionInfo.put("status", "LOGGED_IN");
        } else {
            sessionInfo.put("status", "NOT_LOGGED_IN");
            sessionInfo.put("availableAttributes", Collections.list(session.getAttributeNames()));
        }
        
        return ResponseEntity.ok(sessionInfo);
    }
    
    
 // 🟢 SUBMIT SCORE BY EMAIL - No session required
    // 🟢 SUBMIT SCORE BY EMAIL - With proper null handling
@PostMapping("/submit-score-by-email")
public ResponseEntity<?> submitScoreByEmail(@RequestBody EmailScoreRequest scoreRequest) {
    
    System.out.println("🎯 Received score submission by email:");
    System.out.println("   Email: " + scoreRequest.getEmail());
    System.out.println("   Score: " + scoreRequest.getScore());
    System.out.println("   Level: " + scoreRequest.getLevel());
    System.out.println("   Time: " + scoreRequest.getTimeTaken() + "s");
    System.out.println("   Correct: " + scoreRequest.getCorrectAnswers() + "/" + scoreRequest.getTotalQuestions());
    
    try {
        // Validate request
        if (scoreRequest.getEmail() == null || scoreRequest.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Email is required"));
        }
        
        // Find user by email
        Optional<User> userOptional = userRepository.findByEmail(scoreRequest.getEmail().trim());
        if (userOptional.isEmpty()) {
            System.out.println("❌ User not found with email: " + scoreRequest.getEmail());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("User not found with email: " + scoreRequest.getEmail()));
        }
        
        User user = userOptional.get();
        System.out.println("👤 Saving score for user: " + user.getUsername() + " (ID: " + user.getId() + ")");
        
        // Update user stats before saving score
        updateUserStats(user, scoreRequest);
        
        // Save the score
        EmojiScore savedScore = emojiScoreService.saveScore(
            user.getId(), 
            scoreRequest.getScore(), 
            scoreRequest.getLevel(), 
            scoreRequest.getTimeTaken(), 
            scoreRequest.getCorrectAnswers(), 
            scoreRequest.getTotalQuestions()
        );
        
        System.out.println("✅ Score saved successfully with ID: " + savedScore.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("scoreId", savedScore.getId());
        response.put("message", "Score saved successfully");
        response.put("username", user.getUsername());
        
        return ResponseEntity.ok(response);
        
    } catch (Exception e) {
        System.out.println("❌ Error saving score: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error saving score: " + e.getMessage()));
    }
}

// Helper method to update user statistics
private void updateUserStats(User user, EmailScoreRequest scoreRequest) {
    try {
        // Initialize fields if they are null
        if (user.getGamesPlayed() == null) user.setGamesPlayed(0);
        if (user.getScore() == null) user.setScore(0);
        if (user.getHighestScore() == null) user.setHighestScore(0);
        if (user.getTotalCorrectAnswers() == null) user.setTotalCorrectAnswers(0);
        if (user.getTotalTimePlayed() == null) user.setTotalTimePlayed(0);
        
        // Update user statistics
        user.setGamesPlayed(user.getGamesPlayed() + 1);
        user.setScore(user.getScore() + scoreRequest.getScore());
        
        // Update highest score if current score is higher
        if (scoreRequest.getScore() > user.getHighestScore()) {
            user.setHighestScore(scoreRequest.getScore());
        }
        
        // Update total correct answers and time played
        user.setTotalCorrectAnswers(user.getTotalCorrectAnswers() + scoreRequest.getCorrectAnswers());
        user.setTotalTimePlayed(user.getTotalTimePlayed() + scoreRequest.getTimeTaken());
        
        // Calculate average score
        if (user.getGamesPlayed() > 0) {
            user.setAverageScore((double) user.getScore() / user.getGamesPlayed());
        }
        
        // Calculate accuracy (percentage of correct answers)
        int totalQuestionsAttempted = user.getGamesPlayed() * scoreRequest.getTotalQuestions(); // Approximate
        if (totalQuestionsAttempted > 0) {
            user.setAccuracy((double) user.getTotalCorrectAnswers() / totalQuestionsAttempted * 100);
        }
        
        // Calculate average time per game
        if (user.getGamesPlayed() > 0) {
            user.setAverageTimePerGame((double) user.getTotalTimePlayed() / user.getGamesPlayed());
        }
        
        // Update last played timestamp
        user.setLastPlayed(LocalDateTime.now());
        
        // Save updated user
        userRepository.save(user);
        
        System.out.println("📊 Updated user stats:");
        System.out.println("   Games Played: " + user.getGamesPlayed());
        System.out.println("   Total Score: " + user.getScore());
        System.out.println("   Highest Score: " + user.getHighestScore());
        
    } catch (Exception e) {
        System.out.println("⚠️ Error updating user stats: " + e.getMessage());
        // Don't throw exception - we still want to save the score
    }
}

// 🟢 SIMPLIFIED VERSION - If you're still having issues, use this:
@PostMapping("/submit-score-by-email-simple")
public ResponseEntity<?> submitScoreByEmailSimple(@RequestBody EmailScoreRequest scoreRequest) {
    
    System.out.println("🎯 SIMPLE VERSION: Received score submission by email:");
    System.out.println("   Email: " + scoreRequest.getEmail());
    System.out.println("   Score: " + scoreRequest.getScore());
    
    try {
        // Validate request
        if (scoreRequest.getEmail() == null || scoreRequest.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Email is required"));
        }
        
        // Find user by email
        Optional<User> userOptional = userRepository.findByEmail(scoreRequest.getEmail().trim());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("User not found with email: " + scoreRequest.getEmail()));
        }
        
        User user = userOptional.get();
        
        // SIMPLE FIX: Ensure all fields have values
        initializeUserFields(user);
        
        // Save the score without updating user stats
        EmojiScore savedScore = emojiScoreService.saveScore(
            user.getId(), 
            scoreRequest.getScore(), 
            scoreRequest.getLevel(), 
            scoreRequest.getTimeTaken(), 
            scoreRequest.getCorrectAnswers(), 
            scoreRequest.getTotalQuestions()
        );
        
        System.out.println("✅ Score saved successfully with ID: " + savedScore.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("scoreId", savedScore.getId());
        response.put("message", "Score saved successfully");
        response.put("username", user.getUsername());
        
        return ResponseEntity.ok(response);
        
    } catch (Exception e) {
        System.out.println("❌ Error saving score: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Error saving score: " + e.getMessage()));
    }
}

// Helper method to initialize all user fields
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

    // 🟢 GET USER SCORES BY EMAIL - No session required
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
            List<EmojiScore> scores = emojiScoreService.getUserScores(user.getId());
            
            // Convert to response DTO
            List<Map<String, Object>> scoreResponses = scores.stream()
                .map(this::convertToScoreResponse)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(scoreResponses);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error retrieving scores: " + e.getMessage()));
        }
    }
    
    // 🔧 HELPER METHODS
    private Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
    
    private Map<String, Object> createErrorResponse(String error) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("error", error);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
    
    private Map<String, Object> convertToScoreResponse(EmojiScore score) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", score.getId());
        response.put("score", score.getScore());
        response.put("level", score.getLevel());
        response.put("timeTaken", score.getTimeTaken());
        response.put("correctAnswers", score.getCorrectAnswers());
        response.put("totalQuestions", score.getTotalQuestions());
        response.put("playedAt", score.getPlayedAt());
        response.put("accuracy", score.getTotalQuestions() > 0 ? 
            (double) score.getCorrectAnswers() / score.getTotalQuestions() * 100 : 0.0);
        return response;
    }
    
    private Map<String, Object> getUserGameStats(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            User u = user.get();
            Map<String, Object> stats = new HashMap<>();
            stats.put("level", u.getLevel());
            stats.put("totalScore", u.getScore());
            stats.put("gamesPlayed", u.getGamesPlayed());
            stats.put("highestScore", u.getHighestScore());
            stats.put("averageScore", u.getAverageScore());
            stats.put("accuracy", u.getAccuracy());
            return stats;
        }
        return new HashMap<>();
    }
    
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
    
    // 🟢 REQUEST DTO CLASSES
    public static class ScoreRequest {
        private int score;
        private int level;
        private int timeTaken;
        private int correctAnswers;
        private int totalQuestions;
        
        // Getters and Setters
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
}