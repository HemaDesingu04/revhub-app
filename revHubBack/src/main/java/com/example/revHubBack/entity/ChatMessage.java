package com.example.revHubBack.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "chatMessage")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    @Id
    private String id;
    
    private String senderId;
    private String senderUsername;
    private String receiverId;
    private String receiverUsername;
    private String content;
    private LocalDateTime timestamp;
    private boolean read = false;
    private String messageType = "TEXT"; // TEXT, IMAGE, FILE
}