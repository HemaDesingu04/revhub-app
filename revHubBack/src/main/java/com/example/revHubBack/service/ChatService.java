package com.example.revHubBack.service;

import com.example.revHubBack.entity.ChatMessage;
import com.example.revHubBack.entity.User;
import com.example.revHubBack.repository.ChatMessageRepository;
import com.example.revHubBack.repository.UserRepository;
import com.example.revHubBack.service.NotificationMongoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {
    
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationMongoService notificationService;
    
    public ChatMessage sendMessage(String senderUsername, String receiverUsername, String content) {
        System.out.println("💬 Saving message from " + senderUsername + " to " + receiverUsername);
        
        ChatMessage message = new ChatMessage();
        message.setSenderId(senderUsername);
        message.setSenderUsername(senderUsername);
        message.setReceiverId(receiverUsername);
        message.setReceiverUsername(receiverUsername);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());
        
        ChatMessage savedMessage = chatMessageRepository.save(message);
        System.out.println("✅ Message saved to MongoDB with ID: " + savedMessage.getId());
        
        // Create notification for new message
        try {
            User sender = userRepository.findByUsername(senderUsername).orElse(null);
            User receiver = userRepository.findByUsername(receiverUsername).orElse(null);
            if (sender != null && receiver != null) {
                notificationService.createMessageNotification(receiver, sender, content);
            }
        } catch (Exception e) {
            System.out.println("⚠️ Failed to create message notification: " + e.getMessage());
        }
        
        return savedMessage;
    }
    
    public List<ChatMessage> getConversation(String username1, String username2) {
        try {
            System.out.println("📝 Getting conversation between " + username1 + " and " + username2);
            // Use usernames directly for MongoDB queries
            List<ChatMessage> messages = chatMessageRepository.findConversation(username1, username2);
            System.out.println("✅ Found " + messages.size() + " messages");
            return messages;
        } catch (Exception e) {
            System.out.println("❌ Error getting conversation: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
    
    public void markMessagesAsRead(String receiverUsername, String senderUsername) {
        User receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));
        
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        
        List<ChatMessage> unreadMessages = chatMessageRepository.findConversation(
                sender.getId().toString(), receiver.getId().toString());
        
        unreadMessages.stream()
                .filter(msg -> msg.getReceiverId().equals(receiver.getId().toString()) && !msg.isRead())
                .forEach(msg -> {
                    msg.setRead(true);
                    chatMessageRepository.save(msg);
                });
    }
    
    public List<String> getChatContacts(String username) {
        try {
            System.out.println("📞 Getting chat contacts for " + username);
            // Use username directly for MongoDB queries
            List<ChatMessage> messages = chatMessageRepository.findAllUserChats(username);
            
            // Extract unique contact usernames
            java.util.Set<String> contactSet = new java.util.HashSet<>();
            for (ChatMessage msg : messages) {
                if (!msg.getSenderUsername().equals(username)) {
                    contactSet.add(msg.getSenderUsername());
                }
                if (!msg.getReceiverUsername().equals(username)) {
                    contactSet.add(msg.getReceiverUsername());
                }
            }
            
            System.out.println("✅ Found " + contactSet.size() + " contacts");
            return new java.util.ArrayList<>(contactSet);
        } catch (Exception e) {
            System.out.println("❌ Error getting chat contacts: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
    
    private void createMessageNotification(User receiver, User sender, String content) {
        notificationService.createMessageNotification(receiver, sender, content);
    }
    
    public long getUnreadMessageCount(String receiverUsername, String senderUsername) {
        User receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));
        
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        
        return chatMessageRepository.countUnreadMessages(receiver.getId().toString(), sender.getId().toString());
    }
}