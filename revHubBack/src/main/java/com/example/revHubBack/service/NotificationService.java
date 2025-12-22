package com.example.revHubBack.service;

import com.example.revHubBack.entity.Notification;
import com.example.revHubBack.entity.User;
import com.example.revHubBack.repository.NotificationRepository;
import com.example.revHubBack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Notification> getUserNotifications(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepository.findByUserOrderByCreatedDateDesc(user);
    }

    public Notification markAsRead(Long notificationId, String username) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized");
        }

        notification.setReadStatus(true);
        return notificationRepository.save(notification);
    }

    public void createFollowRequestNotification(User toUser, User fromUser, Long followRequestId) {
        Notification notification = new Notification();
        notification.setUser(toUser);
        notification.setFromUser(fromUser);
        notification.setType("FOLLOW_REQUEST");
        notification.setMessage(fromUser.getUsername() + " wants to follow you");
        notification.setFollowRequestId(followRequestId);
        notificationRepository.save(notification);
    }

    public void createFollowNotification(User toUser, User fromUser) {
        Notification notification = new Notification();
        notification.setUser(toUser);
        notification.setFromUser(fromUser);
        notification.setType("FOLLOW");
        notification.setMessage(fromUser.getUsername() + " started following you");
        notificationRepository.save(notification);
    }

    public void createMentionNotification(User mentionedUser, User mentioner, Long postId) {
        System.out.println("NotificationService: Creating mention notification for " + mentionedUser.getUsername() + " from " + mentioner.getUsername());
        Notification notification = new Notification();
        notification.setUser(mentionedUser);
        notification.setFromUser(mentioner);
        notification.setType("MENTION");
        notification.setMessage(mentioner.getUsername() + " mentioned you in a post");
        notification.setPostId(postId);
        Notification saved = notificationRepository.save(notification);
        System.out.println("NotificationService: Mention notification saved with ID: " + saved.getId());
    }
}