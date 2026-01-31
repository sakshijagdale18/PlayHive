package com.games.repository;

import com.games.model.ShapeShifterScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ShapeShifterScoreRepository extends JpaRepository<ShapeShifterScore, Long> {
    List<ShapeShifterScore> findByUsernameOrderByCreatedAtDesc(String username);
    List<ShapeShifterScore> findTop10ByOrderByScoreDesc();
}