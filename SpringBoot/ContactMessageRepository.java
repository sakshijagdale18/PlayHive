package com.games.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.games.model.ContactMessage;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Integer> {
    List<ContactMessage> findByEmailOrderByTimestampAsc(String email);
}
