package com.example.revHubBack.service;

import com.example.revHubBack.entity.Post;
import com.example.revHubBack.entity.User;
import com.example.revHubBack.repository.PostRepository;
import com.example.revHubBack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SearchService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PostRepository postRepository;

    public Map<String, Object> search(String query) {
        Map<String, Object> results = new HashMap<>();
        
        if (query == null || query.trim().isEmpty()) {
            // Return all users when no query (for @ mentions)
            List<User> allUsers = userRepository.findAll();
            results.put("users", allUsers);
            results.put("posts", List.of());
            return results;
        }
        
        String searchTerm = query.trim().toLowerCase();
        
        // Search users
        List<User> users = userRepository.findByUsernameContainingIgnoreCase(searchTerm);
        
        // Search posts
        List<Post> posts = postRepository.findAllByOrderByCreatedDateDesc().stream()
            .filter(post -> {
                String content = post.getContent().toLowerCase();
                String authorUsername = post.getAuthor().getUsername().toLowerCase();
                
                return content.contains(searchTerm) || 
                       authorUsername.contains(searchTerm) ||
                       (searchTerm.startsWith("#") && content.contains(searchTerm)) ||
                       (!searchTerm.startsWith("#") && content.contains("#" + searchTerm));
            })
            .collect(Collectors.toList());
        
        results.put("users", users);
        results.put("posts", posts);
        return results;
    }
    
    public List<Post> searchByHashtag(String hashtag) {
        String tag = hashtag.startsWith("#") ? hashtag : "#" + hashtag;
        return postRepository.findAllByOrderByCreatedDateDesc().stream()
            .filter(post -> post.getContent().toLowerCase().contains(tag.toLowerCase()))
            .collect(Collectors.toList());
    }
    
    public List<User> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return userRepository.findAll();
        }
        return userRepository.findByUsernameContainingIgnoreCase(query.trim());
    }
}