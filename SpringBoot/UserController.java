package com.games.controller;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.games.model.User;
import com.games.repository.UserRepository;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(
    origins = { "http://localhost:5500", "http://127.0.0.1:5500" },
    allowCredentials = "true"
)
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // 🟢 REGISTER - Now accepts JSON
    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            System.out.println("📝 Register attempt for: " + registerRequest.getEmail());
            
            Optional<User> existingUser = userRepository.findByEmail(registerRequest.getEmail());
            if (existingUser.isPresent()) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "exists");
                response.put("error", "User already exists with this email");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            User newUser = new User();
            newUser.setUsername(registerRequest.getUsername());
            newUser.setEmail(registerRequest.getEmail());
            newUser.setPassword(registerRequest.getPassword());
            userRepository.save(newUser);

            Map<String, String> response = new HashMap<>();
            response.put("message", "success");
            response.put("username", newUser.getUsername());
            response.put("email", newUser.getEmail());
            response.put("id", newUser.getId().toString());
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Registration error: " + e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "error");
            errorResponse.put("error", "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // 🟢 LOGIN - Enhanced with JSON response
    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpSession session) {
        try {
            System.out.println("🔐 Login attempt for email: " + loginRequest.getEmail());
            
            if (loginRequest.getEmail() == null || loginRequest.getPassword() == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "error");
                errorResponse.put("error", "Email and password are required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            Optional<User> user = userRepository.findByEmailAndPassword(
                loginRequest.getEmail().trim(), 
                loginRequest.getPassword()
            );

            if (user.isPresent()) {
                User loggedInUser = user.get();
                
                // ✅ Save user object in session
                session.setAttribute("loggedInUser", loggedInUser);
                session.setMaxInactiveInterval(30 * 60);

                System.out.println("✅ Login successful - Session created:");
                System.out.println("   Session ID: " + session.getId());
                System.out.println("   User: " + loggedInUser.getUsername());

                Map<String, Object> response = new HashMap<>();
                response.put("message", "Login successful");
                response.put("sessionId", session.getId());
                response.put("user", createUserResponse(loggedInUser));
                
                return ResponseEntity.ok(response);
                
            } else {
                System.out.println("❌ Login failed for email: " + loginRequest.getEmail());
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "error");
                errorResponse.put("error", "Invalid credentials");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
        } catch (Exception e) {
            System.err.println("❌ Login error: " + e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "error");
            errorResponse.put("error", "Server error during login: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // 🟢 UPDATE PROFILE - Now accepts JSON
    @PutMapping("/update/{id}")
    @ResponseBody
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateRequest updateRequest) {

        try {
            Optional<User> optionalUser = userRepository.findById(id);
            if (optionalUser.isEmpty()) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "not_found");
                response.put("error", "User not found with id: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            User user = optionalUser.get();
            boolean changed = false;

            if (!Objects.equals(user.getUsername(), updateRequest.getUsername())) {
                user.setUsername(updateRequest.getUsername());
                changed = true;
            }
            if (!Objects.equals(user.getEmail(), updateRequest.getEmail())) {
                user.setEmail(updateRequest.getEmail());
                changed = true;
            }
            if (!Objects.equals(user.getPassword(), updateRequest.getPassword())) {
                user.setPassword(updateRequest.getPassword());
                changed = true;
            }

            if (changed) {
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
                
                Map<String, String> response = new HashMap<>();
                response.put("message", "updated");
                response.put("username", user.getUsername());
                response.put("email", user.getEmail());
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> response = new HashMap<>();
                response.put("message", "no_changes");
                response.put("info", "No changes detected");
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            System.err.println("❌ Update error: " + e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "error");
            errorResponse.put("error", "Update failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // 🟢 GET PROFILE - Now accepts JSON request body
    @PostMapping("/profile")
    @ResponseBody
    public ResponseEntity<?> getUserProfile(@RequestBody ProfileRequest profileRequest) {
        try {
            Optional<User> user = userRepository.findByEmail(profileRequest.getEmail());
            if (user.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "success");
                response.put("user", createUserResponse(user.get()));
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> response = new HashMap<>();
                response.put("message", "not_found");
                response.put("error", "User not found with email: " + profileRequest.getEmail());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            System.err.println("❌ Profile error: " + e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "error");
            errorResponse.put("error", "Profile retrieval failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        Object userObj = session.getAttribute("loggedInUser");
        if (userObj == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Not logged in"));
        }
        
        User user = (User) userObj;
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        
        return ResponseEntity.ok(userInfo);
    }

    // 🟢 LOGOUT
    @PostMapping("/logout")
    @ResponseBody
    public ResponseEntity<?> logout(HttpSession session) {
        try {
            String username = "Unknown";
            User user = (User) session.getAttribute("loggedInUser");
            if (user != null) {
                username = user.getUsername();
            }
            
            session.invalidate();
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "logout_success");
            response.put("info", "User " + username + " logged out successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Logout error: " + e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "error");
            errorResponse.put("error", "Logout failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // 🟢 SESSION CHECK endpoints
    @GetMapping("/session")
    @ResponseBody
    public ResponseEntity<?> checkSession1(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "session_active");
            response.put("user", createUserResponse(user));
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("message", "no_active_session");
            response.put("error", "No active session found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
    
    @GetMapping("/session-check")
    @ResponseBody
    public ResponseEntity<?> checkSession(HttpSession session) {
        Map<String, Object> sessionInfo = new HashMap<>();
        sessionInfo.put("sessionId", session.getId());
        sessionInfo.put("isNew", session.isNew());
        sessionInfo.put("creationTime", session.getCreationTime());
        sessionInfo.put("lastAccessedTime", session.getLastAccessedTime());
        
        Object userObj = session.getAttribute("loggedInUser");
        if (userObj != null) {
            User user = (User) userObj;
            sessionInfo.put("loggedIn", true);
            sessionInfo.put("user", createUserResponse(user));
        } else {
            sessionInfo.put("loggedIn", false);
            sessionInfo.put("availableAttributes", Collections.list(session.getAttributeNames()));
        }
        
        System.out.println("🔍 Session Check:");
        System.out.println("   Session ID: " + sessionInfo.get("sessionId"));
        System.out.println("   Logged In: " + sessionInfo.get("loggedIn"));
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "session_check_complete");
        response.put("sessionInfo", sessionInfo);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/debug-session")
    @ResponseBody
    public ResponseEntity<?> debugSession(HttpSession session) {
        Map<String, Object> debugInfo = new HashMap<>();
        debugInfo.put("sessionId", session.getId());
        debugInfo.put("creationTime", session.getCreationTime());
        debugInfo.put("lastAccessedTime", session.getLastAccessedTime());
        debugInfo.put("maxInactiveInterval", session.getMaxInactiveInterval());
        
        List<String> attributeNames = Collections.list(session.getAttributeNames());
        debugInfo.put("attributeNames", attributeNames);
        
        User user = (User) session.getAttribute("loggedInUser");
        debugInfo.put("userInSession", user != null ? user.getUsername() : "null");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "debug_info");
        response.put("debugInfo", debugInfo);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    @ResponseBody
    public ResponseEntity<?> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Backend is running! ✅");
        response.put("status", "healthy");
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    // 🟢 Helper method to create user response
    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", user.getId());
        userResponse.put("username", user.getUsername());
        userResponse.put("email", user.getEmail());
        userResponse.put("level", user.getLevel());
        userResponse.put("score", user.getScore());
        userResponse.put("gamesPlayed", user.getGamesPlayed());
        userResponse.put("highestScore", user.getHighestScore());
        userResponse.put("averageScore", user.getAverageScore());
        userResponse.put("registrationDate", user.getRegistrationDate());
        userResponse.put("lastPlayed", user.getLastPlayed());
        return userResponse;
    }

    // 🟢 Request DTO Classes
    public static class LoginRequest {
        private String email;
        private String password;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    public static class RegisterRequest {
        private String username;
        private String email;
        private String password;
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    public static class UpdateRequest {
        private String username;
        private String email;
        private String password;
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    public static class ProfileRequest {
        private String email;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}