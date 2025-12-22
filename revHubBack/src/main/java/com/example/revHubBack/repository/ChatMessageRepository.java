package com.example.revHubBack.repository;

import com.example.revHubBack.entity.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    
    @Query("{ $or: [ { $and: [ { 'senderUsername': ?0 }, { 'receiverUsername': ?1 } ] }, { $and: [ { 'senderUsername': ?1 }, { 'receiverUsername': ?0 } ] } ] }")
    List<ChatMessage> findConversation(String username1, String username2);
    
    List<ChatMessage> findByReceiverIdAndReadFalse(String receiverId);
    
    @Query("{ $or: [ { 'senderUsername': ?0 }, { 'receiverUsername': ?0 } ] }")
    List<ChatMessage> findByUserIdInvolved(String username);
    
    @Query("{ $or: [ { 'senderUsername': ?0 }, { 'receiverUsername': ?0 } ] }")
    List<ChatMessage> findChatContactsRaw(String username);
    
    @Query(value = "{ $or: [ { 'senderUsername': ?0 }, { 'receiverUsername': ?0 } ] }", sort = "{ 'timestamp': -1 }")
    List<ChatMessage> findAllUserChats(String username);
    
    @Query(value = "{ 'receiverId': ?0, 'senderId': ?1, 'read': false }", count = true)
    long countUnreadMessages(String receiverId, String senderId);
}