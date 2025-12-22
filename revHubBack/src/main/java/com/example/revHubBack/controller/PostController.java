package com.example.revHubBack.controller;

import com.example.revHubBack.dto.PostRequest;
import com.example.revHubBack.entity.Post;
import com.example.revHubBack.service.PostService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts")
public class PostController {
    @Autowired
    private PostService postService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "universal") String feedType,
            Authentication authentication) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            String currentUsername = authentication != null ? authentication.getName() : null;
            Page<Post> posts;
            
            if ("followers".equals(feedType)) {
                posts = postService.getFollowersPosts(pageable, currentUsername);
            } else {
                posts = postService.getUniversalPosts(pageable);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", posts.getContent());
            response.put("totalElements", posts.getTotalElements());
            response.put("totalPages", posts.getTotalPages());
            response.put("size", posts.getSize());
            response.put("number", posts.getNumber());
            response.put("feedType", feedType);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("content", new ArrayList<>());
            errorResponse.put("totalElements", 0);
            errorResponse.put("totalPages", 0);
            errorResponse.put("size", 0);
            errorResponse.put("number", 0);
            errorResponse.put("feedType", feedType);
            return ResponseEntity.ok(errorResponse);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        return postService.getPostById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/upload", consumes = {"multipart/form-data"})
    public ResponseEntity<Post> createPostWithFile(
            @RequestParam("content") String content,
            @RequestParam(value = "file", required = false) org.springframework.web.multipart.MultipartFile file,
            @RequestParam(value = "visibility", defaultValue = "PUBLIC") String visibility,
            Authentication authentication) {
        try {
            Post post = postService.createPost(content, file, authentication.getName(), visibility);
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping(consumes = {"application/json"})
    public ResponseEntity<Post> createPostJson(@Valid @RequestBody PostRequest postRequest, Authentication authentication) {
        try {
            String visibility = postRequest.getVisibility() != null ? postRequest.getVisibility() : "PUBLIC";
            Post post = postService.createPost(postRequest.getContent(), null, authentication.getName(), visibility);
            if (postRequest.getImageUrl() != null && !postRequest.getImageUrl().isEmpty()) {
                post.setImageUrl(postRequest.getImageUrl());
                if (postRequest.getMediaType() != null && !postRequest.getMediaType().isEmpty()) {
                    post.setMediaType(postRequest.getMediaType());
                } else if (postRequest.getImageUrl().startsWith("data:video/")) {
                    post.setMediaType("video");
                } else if (postRequest.getImageUrl().startsWith("data:image/")) {
                    post.setMediaType("image");
                }
            }
            post = postService.savePost(post);
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(@PathVariable Long id, @Valid @RequestBody PostRequest postRequest, Authentication authentication) {
        try {
            Post updatedPost = postService.updatePost(id, postRequest, authentication.getName());
            return ResponseEntity.ok(updatedPost);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id, Authentication authentication) {
        try {
            postService.deletePost(id, authentication.getName());
            return ResponseEntity.ok("Post deleted successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Post>> searchPosts(@RequestParam String query) {
        try {
            List<Post> posts = postService.searchPosts(query);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
    
    @PostMapping("/test-mention")
    public ResponseEntity<String> testMention(@RequestParam String content, Authentication authentication) {
        try {
            System.out.println("Testing mention with content: " + content);
            Post post = postService.createPost(content, null, authentication.getName(), "PUBLIC");
            return ResponseEntity.ok("Post created with ID: " + post.getId() + ". Check console for mention processing logs.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

}