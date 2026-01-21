package com.example.demo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String text;
    public String sender;   // Who sent it?
    public String receiver; // Who is it for? (Null = Group Chat)
    
    public LocalDateTime timestamp = LocalDateTime.now();
}