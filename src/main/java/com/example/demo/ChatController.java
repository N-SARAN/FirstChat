package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
// FIX: Allow both your Localhost (for testing) and Vercel (for production)
@CrossOrigin(origins = {
    "http://localhost:4200",
    "https://first-chat-murex.vercel.app",
    "https://first-chat-iqfq4vp0g-sarans-projects-fc55938d.vercel.app" 
})
public class ChatController {

    @Autowired private MessageRepository msgRepo;
    @Autowired private UserRepository userRepo;

    // 1. LOGIN / REGISTER
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginUser) {
        User user = userRepo.findByUsername(loginUser.username);
        
        // Auto-register if new
        if (user == null) {
            User newUser = userRepo.save(loginUser);
            return ResponseEntity.ok(newUser);
        }
        
        // Check Password
        if (!user.password.equals(loginUser.password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Password");
        }
        
        return ResponseEntity.ok(user);
    }

    // 2. GET ALL USERS
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    // 3. GET MESSAGES
    @GetMapping("/messages")
    public List<Message> getMessages(@RequestParam(required = false) String user1, 
                                     @RequestParam(required = false) String user2) {
        List<Message> all = msgRepo.findAll();
        
        if (user1 == null || user2 == null) {
            return all.stream().filter(m -> m.receiver == null).collect(Collectors.toList());
        } else {
            return all.stream().filter(m -> 
                (m.sender.equals(user1) && m.receiver.equals(user2)) || 
                (m.sender.equals(user2) && m.receiver.equals(user1))
            ).collect(Collectors.toList());
        }
    }

    // 4. SEND MESSAGE
    @PostMapping("/messages")
    public Message sendMessage(@RequestBody Message msg) {
        return msgRepo.save(msg);
    }
}