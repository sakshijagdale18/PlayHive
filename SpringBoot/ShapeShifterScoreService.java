package com.games.service;

import com.games.model.ShapeShifterScore;
import com.games.repository.ShapeShifterScoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ShapeShifterScoreService {

    @Autowired
    private ShapeShifterScoreRepository repository;

    public ShapeShifterScore saveScore(ShapeShifterScore score) {
        return repository.save(score);
    }

    public List<ShapeShifterScore> getAllScores() {
        return repository.findAll();
    }

    public List<ShapeShifterScore> getTopScores() {
        return repository.findTop10ByOrderByScoreDesc();
    }

    public List<ShapeShifterScore> getUserScores(String username) {
        return repository.findByUsernameOrderByCreatedAtDesc(username);
    }

    public void deleteScore(Long id) {
        repository.deleteById(id);
    }
}