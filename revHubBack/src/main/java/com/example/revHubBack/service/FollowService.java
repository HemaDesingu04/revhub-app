package com.example.revHubBack.service;

import com.example.revHubBack.entity.Follow;
import com.example.revHubBack.entity.User;
import com.example.revHubBack.repository.FollowRepository;
import com.example.revHubBack.repository.UserRepository;
import com.example.revHubBack.service.NotificationMongoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FollowService {
    
    @Autowired
    private FollowRepository followRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationMongoService notificationService;
    
    public String followUser(String currentUsername, String targetUsername) {
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        
        User targetUser = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new RuntimeException("Target user not found"));
        
        if (currentUser.getId().equals(targetUser.getId())) {
            throw new RuntimeException("Cannot follow yourself");
        }
        
        Optional<Follow> existingFollow = followRepository.findByFollowerAndFollowing(currentUser, targetUser);
        
        if (existingFollow.isPresent()) {
            Follow follow = existingFollow.get();
            if (follow.getStatus() == Follow.FollowStatus.ACCEPTED) {
                throw new RuntimeException("Already following this user");
            } else {
                throw new RuntimeException("Follow request already sent");
            }
        }
        
        Follow follow = new Follow();
        follow.setFollower(currentUser);
        follow.setFollowing(targetUser);
        
        if (targetUser.getIsPrivate()) {
            follow.setStatus(Follow.FollowStatus.PENDING);
            Follow savedFollow = followRepository.save(follow);
            notificationService.createFollowRequestNotification(targetUser, currentUser, savedFollow.getId());
            return "Follow request sent";
        } else {
            follow.setStatus(Follow.FollowStatus.ACCEPTED);
            followRepository.save(follow);
            notificationService.createFollowNotification(targetUser, currentUser);
            return "Now following " + targetUsername;
        }
    }
    
    public void unfollowUser(String currentUsername, String targetUsername) {
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        
        User targetUser = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new RuntimeException("Target user not found"));
        
        Optional<Follow> follow = followRepository.findByFollowerAndFollowing(currentUser, targetUser);
        
        if (follow.isPresent()) {
            followRepository.delete(follow.get());
        } else {
            throw new RuntimeException("Not following this user");
        }
    }
    
    public void acceptFollowRequest(String currentUsername, Long followId) {
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Follow follow = followRepository.findById(followId)
                .orElseThrow(() -> new RuntimeException("Follow request not found"));
        
        if (!follow.getFollowing().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized to accept this request");
        }
        
        if (follow.getStatus() != Follow.FollowStatus.PENDING) {
            throw new RuntimeException("Follow request is not pending");
        }
        
        follow.setStatus(Follow.FollowStatus.ACCEPTED);
        followRepository.save(follow);
        
        notificationService.removeFollowRequestNotification(followId);
        
        notificationService.createFollowNotification(follow.getFollowing(), follow.getFollower());
    }
    
    public void rejectFollowRequest(String currentUsername, Long followId) {
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Follow follow = followRepository.findById(followId)
                .orElseThrow(() -> new RuntimeException("Follow request not found"));
        
        if (!follow.getFollowing().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized to reject this request");
        }
        
        if (follow.getStatus() != Follow.FollowStatus.PENDING) {
            throw new RuntimeException("Follow request is not pending");
        }
        
        followRepository.delete(follow);
        
        notificationService.removeFollowRequestNotification(followId);
    }
    
    public List<Follow> getPendingFollowRequests(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return followRepository.findPendingFollowRequests(user);
    }
    
    public String getFollowStatus(String currentUsername, String targetUsername) {
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        
        User targetUser = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new RuntimeException("Target user not found"));
        
        Optional<Follow> follow = followRepository.findByFollowerAndFollowing(currentUser, targetUser);
        
        if (follow.isPresent()) {
            return follow.get().getStatus().toString();
        }
        
        return "NOT_FOLLOWING";
    }
    
    public void cancelFollowRequest(String currentUsername, String targetUsername) {
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        
        User targetUser = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new RuntimeException("Target user not found"));
        
        Optional<Follow> follow = followRepository.findByFollowerAndFollowing(currentUser, targetUser);
        
        if (follow.isPresent() && follow.get().getStatus() == Follow.FollowStatus.PENDING) {
            followRepository.delete(follow.get());
        } else {
            throw new RuntimeException("No pending follow request found");
        }
    }
    
    public void removeFollower(String currentUsername, String followerUsername) {
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User follower = userRepository.findByUsername(followerUsername)
                .orElseThrow(() -> new RuntimeException("Follower not found"));
        
        Optional<Follow> follow = followRepository.findByFollowerAndFollowing(follower, currentUser);
        
        if (follow.isPresent()) {
            Follow followRelation = follow.get();
            followRepository.delete(followRelation);
        } else {
            throw new RuntimeException("This user is not following you");
        }
    }
}