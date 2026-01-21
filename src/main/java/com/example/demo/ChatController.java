package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class ChatController {

    @Autowired private MessageRepository msgRepo;
    @Autowired private UserRepository userRepo;

    // 1. LOGIN / REGISTER
    @PostMapping("/login")
    public User login(@RequestBody User loginUser) {
        User user = userRepo.findByUsername(loginUser.username);
        if (user == null) {
            return userRepo.save(loginUser); // Auto-register if new
        }
        if (!user.password.equals(loginUser.password)) {
            throw new RuntimeException("Wrong password");
        }
        return user;
    }

    // 2. GET ALL USERS (For Contact List)
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    // 3. GET MESSAGES (Logic for Group vs Private)
    @GetMapping("/messages")
    public List<Message> getMessages(@RequestParam(required = false) String user1, 
                                     @RequestParam(required = false) String user2) {
        List<Message> all = msgRepo.findAll();
        
        if (user1 == null || user2 == null) {
            // Return only GROUP messages (where receiver is null)
            return all.stream().filter(m -> m.receiver == null).collect(Collectors.toList());
        } else {
            // Return PRIVATE messages between user1 and user2
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