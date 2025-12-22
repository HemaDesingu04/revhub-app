package com.example.revHubBack.service;

import com.example.revHubBack.entity.NotificationMongo;
import com.example.revHubBack.entity.User;
import com.example.revHubBack.repository.NotificationMongoRepository;
import com.example.revHubBack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationMongoService {
    
    @Autowired
    private NotificationMongoRepository notificationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public List<NotificationMongo> getUserNotifications(String username) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            return notificationRepository.findByUserIdOrderByCreatedDateDesc(user.getId().toString());
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }
    
    public NotificationMongo markAsRead(String notificationId, String username) {
        NotificationMongo notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!notification.getUserId().equals(user.getId().toString())) {
            throw new RuntimeException("Unauthorized");
        }
        
        notification.setReadStatus(true);
        return notificationRepository.save(notification);
    }
    
    public void createFollowRequestNotification(User toUser, User fromUser, Long followRequestId) {
        NotificationMongo notification = new NotificationMongo();
        notification.setUserId(toUser.getId().toString());
        notification.setFromUserId(fromUser.getId().toString());
        notification.setFromUsername(fromUser.getUsername());
        notification.setFromUserProfilePicture(fromUser.getProfilePicture());
        notification.setType("FOLLOW_REQUEST");
        notification.setMessage(fromUser.getUsername() + " wants to follow you");
        notification.setFollowRequestId(followRequestId);
        notification.setCreatedDate(LocalDateTime.now());
        
        notificationRepository.save(notification);
    }
    
    public void createFollowNotification(User toUser, User fromUser) {
        NotificationMongo notification = new NotificationMongo();
        notification.setUserId(toUser.getId().toString());
        notification.setFromUserId(fromUser.getId().toString());
        notification.setFromUsername(fromUser.getUsername());
        notification.setFromUserProfilePicture(fromUser.getProfilePicture());
        notification.setType("FOLLOW");
        notification.setMessage(fromUser.getUsername() + " started following you");
        notification.setCreatedDate(LocalDateTime.now());
        
        notificationRepository.save(notification);
    }
    
    public long getUnreadCount(String username) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            return notificationRepository.countByUserIdAndReadStatusFalse(user.getId().toString());
        } catch (Exception e) {
            return 0L;
        }
    }
    
    public void removeFollowRequestNotification(Long followRequestId) {
        try {
            List<NotificationMongo> notifications = notificationRepository.findAll();
            notifications.stream()
                .filter(n -> followRequestId.equals(n.getFollowRequestId()))
                .forEach(notificationRepository::delete);
        } catch (Exception e) {
        }
    }
    
    public void createLikeNotification(User postOwner, User liker, Long postId) {
        if (postOwner.getId().equals(liker.getId())) {
            return;
        }
        
        NotificationMongo notification = new NotificationMongo();
        notification.setUserId(postOwner.getId().toString());
        notification.setFromUserId(liker.getId().toString());
        notification.setFromUsername(liker.getUsername());
        notification.setFromUserProfilePicture(liker.getProfilePicture());
        notification.setType("LIKE");
        notification.setMessage(liker.getUsername() + " liked your post");
        notification.setPostId(postId);
        notification.setCreatedDate(LocalDateTime.now());
        
        notificationRepository.save(notification);
    }
    
    public void createMentionNotification(User mentionedUser, User mentioner, Long postId, String content) {
        System.out.println("NotificationMongoService: Creating mention notification for " + mentionedUser.getUsername() + " from " + mentioner.getUsername());
        NotificationMongo notification = new NotificationMongo();
        notification.setUserId(mentionedUser.getId().toString());
        notification.setFromUserId(mentioner.getId().toString());
        notification.setFromUsername(mentioner.getUsername());
        notification.setFromUserProfilePicture(mentioner.getProfilePicture());
        notification.setType("MENTION");
        notification.setMessage(mentioner.getUsername() + " mentioned you in a post");
        notification.setPostId(postId);
        notification.setCreatedDate(LocalDateTime.now());
        
        NotificationMongo saved = notificationRepository.save(notification);
        System.out.println("NotificationMongoService: Mention notification saved with ID: " + saved.getId());
    }
    
    public void deleteNotification(String notificationId, String username) {
        NotificationMongo notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!notification.getUserId().equals(user.getId().toString())) {
            throw new RuntimeException("Unauthorized to delete this notification");
        }
        
        notificationRepository.delete(notification);
    }
    
    public void createMessageNotification(User receiver, User sender, String content) {
        NotificationMongo notification = new NotificationMongo();
        notification.setUserId(receiver.getId().toString());
        notification.setFromUserId(sender.getId().toString());
        notification.setFromUsername(sender.getUsername());
        notification.setFromUserProfilePicture(sender.getProfilePicture());
        notification.setType("MESSAGE");
        notification.setMessage(sender.getUsername() + " sent you a message: " + 
            (content.length() > 30 ? content.substring(0, 30) + "..." : content));
        notification.setCreatedDate(LocalDateTime.now());
        
        notificationRepository.save(notification);
    }
    
    public void createCommentNotification(User postOwner, User commenter, Long postId, String content) {
        if (postOwner.getId().equals(commenter.getId())) {
            return;
        }
        
        NotificationMongo notification = new NotificationMongo();
        notification.setUserId(postOwner.getId().toString());
        notification.setFromUserId(commenter.getId().toString());
        notification.setFromUsername(commenter.getUsername());
        notification.setFromUserProfilePicture(commenter.getProfilePicture());
        notification.setType("COMMENT");
        notification.setMessage(commenter.getUsername() + " commented on your post");
        notification.setPostId(postId);
        notification.setCommentId(null);
        notification.setCreatedDate(LocalDateTime.now());
        
        notificationRepository.save(notification);
    }
}