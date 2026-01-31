package com.games.repository;

import com.games.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndPassword(String email, String password);

    @Query("SELECT COUNT(u) FROM User u WHERE u.registrationDate BETWEEN :start AND :end")
    Long countRegistrationsBetween(LocalDate start, LocalDate end);

    List<User> findTop10ByOrderByScoreDesc();
}
