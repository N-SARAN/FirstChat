package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // 1. Find Global Messages (Where receiver is NULL)
    List<Message> findByReceiverIsNull();

    // 2. Find Private Chat (A -> B OR B -> A)
    @Query("SELECT m FROM Message m WHERE (m.sender = :user1 AND m.receiver = :user2) OR (m.sender = :user2 AND m.receiver = :user1)")
    List<Message> findPrivateChat(@Param("user1") String user1, @Param("user2") String user2);
}