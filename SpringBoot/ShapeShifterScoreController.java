package com.games.controller;

import com.games.model.ShapeShifterScore;
import com.games.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/shapeshifter")
@CrossOrigin(origins = "*") // allow frontend access
public class ShapeShifterScoreController {

    @Autowired
    private ShapeShifterScoreService service;

    @PostMapping("/save")
    public ShapeShifterScore saveScore(@RequestBody ShapeShifterScore score) {
        return service.saveScore(score);
    }

    @GetMapping("/scores")
    public List<ShapeShifterScore> getAllScores() {
        return service.getAllScores();
    }

    @GetMapping("/top")
    public List<ShapeShifterScore> getTopScores() {
        return service.getTopScores();
    }

    @GetMapping("/user/{username}")
    public List<ShapeShifterScore> getUserScores(@PathVariable String username) {
        return service.getUserScores(username);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteScore(@PathVariable Long id) {
        service.deleteScore(id);
    }
}