package com.example.revHubBack.controller;

import com.example.revHubBack.entity.NotificationMongo;
import com.example.revHubBack.service.FollowService;
import com.example.revHubBack.service.NotificationMongoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @Autowired
    private NotificationMongoService notificationService;
    
    @Autowired
    private FollowService followService;

    @GetMapping
    public ResponseEntity<List<NotificationMongo>> getNotifications(Authentication authentication) {
        try {
            List<NotificationMongo> notifications = notificationService.getUserNotifications(authentication.getName());
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.ok(new java.util.ArrayList<>());
        }
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationMongo> markAsRead(@PathVariable String id, Authentication authentication) {
        try {
            NotificationMongo notification = notificationService.markAsRead(id, authentication.getName());
            return ResponseEntity.ok(notification);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        try {
            long count = notificationService.getUnreadCount(authentication.getName());
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.ok(0L);
        }
    }
    
    @PostMapping("/follow-request/{followId}/accept")
    public ResponseEntity<String> acceptFollowRequest(@PathVariable Long followId, Authentication authentication) {
        try {
            followService.acceptFollowRequest(authentication.getName(), followId);
            return ResponseEntity.ok("Follow request accepted");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/follow-request/{followId}/reject")
    public ResponseEntity<String> rejectFollowRequest(@PathVariable Long followId, Authentication authentication) {
        try {
            followService.rejectFollowRequest(authentication.getName(), followId);
            return ResponseEntity.ok("Follow request rejected");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteNotification(@PathVariable String id, Authentication authentication) {
        try {
            notificationService.deleteNotification(id, authentication.getName());
            return ResponseEntity.ok("Notification deleted");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}