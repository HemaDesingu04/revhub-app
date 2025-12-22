package com.example.revHubBack.service;

import com.example.revHubBack.entity.Follow;
import com.example.revHubBack.entity.Post;
import com.example.revHubBack.entity.PostVisibility;
import com.example.revHubBack.entity.User;
import com.example.revHubBack.repository.FollowRepository;
import com.example.revHubBack.repository.PostRepository;
import com.example.revHubBack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeedService {
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FollowRepository followRepository;

    public Page<Post> getPersonalizedFeed(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get followed users
        List<User> followedUsers = followRepository.findFollowing(user);
        
        // Add user's own posts
        followedUsers.add(user);
        
        List<Post> feedPosts = new ArrayList<>();
        
        // Get posts from followed users (last 7 days for performance)
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        for (User followedUser : followedUsers) {
            List<Post> userPosts = postRepository.findByAuthorAndCreatedDateAfterOrderByCreatedDateDesc(
                followedUser, weekAgo);
            feedPosts.addAll(userPosts.stream()
                .filter(post -> post.getVisibility() == PostVisibility.PUBLIC || 
                               post.getAuthor().equals(user) ||
                               followRepository.existsByFollowerAndFollowing(user, post.getAuthor()))
                .collect(Collectors.toList()));
        }
        
        // Add popular posts (high engagement) from last 24 hours
        LocalDateTime dayAgo = LocalDateTime.now().minusDays(1);
        List<Post> popularPosts = postRepository.findByCreatedDateAfterOrderByCreatedDateDesc(dayAgo)
                .stream()
                .filter(post -> post.getVisibility() == PostVisibility.PUBLIC)
                .filter(post -> (post.getLikesCount() + post.getCommentsCount() + post.getSharesCount()) >= 5)
                .filter(post -> !followedUsers.contains(post.getAuthor())) // Avoid duplicates
                .collect(Collectors.toList());
        
        feedPosts.addAll(popularPosts);
        
        // Sort by engagement score and recency
        List<Post> sortedFeed = feedPosts.stream()
                .distinct()
                .sorted(Comparator.comparing(this::calculateEngagementScore).reversed()
                        .thenComparing(Post::getCreatedDate, Comparator.reverseOrder()))
                .collect(Collectors.toList());
        
        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), sortedFeed.size());
        List<Post> paginatedPosts = start < sortedFeed.size() ? 
                sortedFeed.subList(start, end) : new ArrayList<>();
        
        return new PageImpl<>(paginatedPosts, pageable, sortedFeed.size());
    }
    
    private double calculateEngagementScore(Post post) {
        int likes = post.getLikesCount();
        int comments = post.getCommentsCount() * 2; // Comments worth more
        int shares = post.getSharesCount() * 3; // Shares worth most
        
        // Time decay factor (newer posts get higher score)
        long hoursOld = java.time.Duration.between(post.getCreatedDate(), LocalDateTime.now()).toHours();
        double timeDecay = Math.max(0.1, 1.0 - (hoursOld / 168.0)); // Decay over a week
        
        return (likes + comments + shares) * timeDecay;
    }
}