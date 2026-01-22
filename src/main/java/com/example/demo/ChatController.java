package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity; // Import this
import org.springframework.http.HttpStatus;     // Import this
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
// 1. FIX: Allow your Vercel frontend (or use "*" to allow everyone for testing)
@CrossOrigin(origins = {"http://localhost:4200", "https://first-chat-murex.vercel.app"}) 
public class ChatController {

    @Autowired private MessageRepository msgRepo;
    @Autowired private UserRepository userRepo;

    // 2. LOGIN / REGISTER
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginUser) {
        User user = userRepo.findByUsername(loginUser.username);
        
        // Auto-register logic
        if (user == null) {
            User newUser = userRepo.save(loginUser);
            return ResponseEntity.ok(newUser);
        }
        
        // Check Password
        if (!user.password.equals(loginUser.password)) {
            // 3. IMPROVEMENT: Return 401 Unauthorized instead of crashing
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Password");
        }
        
        return ResponseEntity.ok(user);
    }

    // ... (Keep the rest of your methods the same) ...
    
    // GET ALL USERS
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    // GET MESSAGES
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

    // SEND MESSAGE
    @PostMapping("/messages")
    public Message sendMessage(@RequestBody Message msg) {
        return msgRepo.save(msg);
    }
}