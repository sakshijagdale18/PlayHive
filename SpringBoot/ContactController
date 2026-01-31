package com.games.controller;

import com.games.model.ContactMessage;
import com.games.repository.ContactMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/contact")
@CrossOrigin(origins = "*")
public class ContactController {

    @Autowired
    private ContactMessageRepository contactRepo;

    // Save contact message
    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestParam String name,
                                              @RequestParam String email,
                                              @RequestParam String message) {
        ContactMessage msg = new ContactMessage();
        msg.setName(name);
        msg.setEmail(email);
        msg.setMessage(message);
        contactRepo.save(msg);
        return ResponseEntity.ok("saved");
    }

    // Admin fetch all messages
    @GetMapping("/all")
    public List<ContactMessage> getAllMessages() {
        return contactRepo.findAll();
    }

    // Admin reply to a message
    @PutMapping("/reply/{id}")
    public ResponseEntity<String> replyToMessage(@PathVariable int id, @RequestParam String reply) {
        Optional<ContactMessage> msg = contactRepo.findById(id);
        if (msg.isPresent()) {
            ContactMessage contact = msg.get();
            contact.setAdminReply(reply);
            contactRepo.save(contact);
            return ResponseEntity.ok("replied");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("not_found");
        }
    }

    // Fetch conversation by email
    @GetMapping("/messages")
    public List<ContactMessage> getMessagesByEmail(@RequestParam String email) {
        return contactRepo.findByEmailOrderByTimestampAsc(email);
    }
}
