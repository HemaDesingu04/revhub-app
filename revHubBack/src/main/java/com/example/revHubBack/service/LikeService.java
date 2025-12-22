package com.example.revHubBack.service;

import com.example.revHubBack.entity.Like;
import com.example.revHubBack.entity.Post;
import com.example.revHubBack.entity.User;
import com.example.revHubBack.repository.LikeRepository;
import com.example.revHubBack.repository.PostRepository;
import com.example.revHubBack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeService {
    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationMongoService notificationService;

    @Transactional
    public java.util.Map<String, Object> toggleLike(Long postId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        boolean isLiked;
        if (likeRepository.existsByUserAndPost(user, post)) {
            Like like = likeRepository.findByUserAndPost(user, post).get();
            likeRepository.delete(like);
            post.setLikesCount(Math.max(0, post.getLikesCount() - 1));
            isLiked = false;
        } else {
            Like like = new Like();
            like.setUser(user);
            like.setPost(post);
            likeRepository.save(like);
            post.setLikesCount(post.getLikesCount() + 1);
            isLiked = true;
            
            if (!post.getAuthor().getId().equals(user.getId())) {
                notificationService.createLikeNotification(post.getAuthor(), user, postId);
            }
        }
        postRepository.save(post);
        
        return java.util.Map.of(
            "likesCount", post.getLikesCount(),
            "isLiked", isLiked
        );
    }
}