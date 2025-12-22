package com.example.revHubBack.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "notificationMongo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMongo {
    @Id
    private String id;
    
    private String userId;
    private String type; // FOLLOW_REQUEST, FOLLOW, LIKE, COMMENT, MENTION
    private String message;
    private boolean readStatus = false;
    private LocalDateTime createdDate;
    
    // Additional data for different notification types
    private String fromUserId;
    private String fromUsername;
    private String fromUserProfilePicture;
    private Long followRequestId;
    private Long postId;
    private Long commentId;
}