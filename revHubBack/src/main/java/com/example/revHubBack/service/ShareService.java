package com.example.revHubBack.service;

import com.example.revHubBack.entity.Share;
import com.example.revHubBack.entity.Post;
import com.example.revHubBack.entity.User;
import com.example.revHubBack.repository.ShareRepository;
import com.example.revHubBack.repository.PostRepository;
import com.example.revHubBack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShareService {
    @Autowired
    private ShareRepository shareRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public String sharePost(Long postId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!shareRepository.existsByUserAndPost(user, post)) {
            Share share = new Share();
            share.setUser(user);
            share.setPost(post);
            shareRepository.save(share);
            
            post.setSharesCount(post.getSharesCount() + 1);
            postRepository.save(post);
            return "Post shared successfully!";
        } else {
            return "Post already shared!";
        }
    }
}