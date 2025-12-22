package com.example.revHubBack.service;

import com.example.revHubBack.dto.PostRequest;
import com.example.revHubBack.entity.Post;
import com.example.revHubBack.entity.User;
import com.example.revHubBack.entity.Comment;
import com.example.revHubBack.entity.Like;
import com.example.revHubBack.entity.Share;
import com.example.revHubBack.repository.PostRepository;
import com.example.revHubBack.repository.UserRepository;
import com.example.revHubBack.repository.CommentRepository;
import com.example.revHubBack.repository.FollowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private NotificationMongoService notificationService;
    
    @Autowired
    private NotificationService notificationServiceSQL;
    
    @Autowired
    private FollowRepository followRepository;

    public Page<Post> getUniversalPosts(Pageable pageable) {
        return postRepository.findPublicPosts(pageable);
    }
    
    public Page<Post> getFollowersPosts(Pageable pageable, String currentUsername) {
        if (currentUsername == null) {
            return postRepository.findPublicPosts(pageable);
        }
        
        User currentUser = userRepository.findByUsername(currentUsername).orElse(null);
        if (currentUser == null) {
            return postRepository.findPublicPosts(pageable);
        }
        
        List<User> following = followRepository.findFollowing(currentUser);
        List<Long> followingIds = following.stream()
            .map(User::getId)
            .collect(java.util.stream.Collectors.toList());
        
        // Add current user's ID to see their own posts in followers feed
        followingIds.add(currentUser.getId());
        
        return postRepository.findFollowersPosts(currentUser.getId(), followingIds, pageable);
    }

    public Optional<Post> getPostById(Long id) {
        return postRepository.findById(id);
    }

    public Post createPost(String content, org.springframework.web.multipart.MultipartFile file, String username, String visibility) {
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = new Post();
        post.setContent(content);
        
        if (file != null && !file.isEmpty()) {
            try {
                byte[] fileBytes = file.getBytes();
                String base64File = java.util.Base64.getEncoder().encodeToString(fileBytes);
                String mimeType = file.getContentType();
                String dataUrl = "data:" + mimeType + ";base64," + base64File;
                post.setImageUrl(dataUrl);
                
                if (mimeType != null) {
                    if (mimeType.startsWith("image/")) {
                        post.setMediaType("image");
                    } else if (mimeType.startsWith("video/")) {
                        post.setMediaType("video");
                    }
                }
                

            } catch (Exception e) {
                throw new RuntimeException("Error processing file: " + e.getMessage());
            }
        }
        
        post.setAuthor(author);
        
        // Set visibility
        if (visibility != null) {
            try {
                post.setVisibility(com.example.revHubBack.entity.PostVisibility.valueOf(visibility.toUpperCase()));
            } catch (IllegalArgumentException e) {
                post.setVisibility(com.example.revHubBack.entity.PostVisibility.PUBLIC);
            }
        }
        
        Post savedPost = postRepository.save(post);
        
        System.out.println("Processing mentions for post: " + savedPost.getContent());
        processMentions(savedPost, author);
        
        return savedPost;
    }

    public Post updatePost(Long id, PostRequest postRequest, String username) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getAuthor().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized to edit this post");
        }

        post.setContent(postRequest.getContent());
        if (postRequest.getImageUrl() != null) {
            post.setImageUrl(postRequest.getImageUrl());
        }
        if (postRequest.getVisibility() != null) {
            try {
                post.setVisibility(com.example.revHubBack.entity.PostVisibility.valueOf(postRequest.getVisibility().toUpperCase()));
            } catch (IllegalArgumentException e) {
                post.setVisibility(com.example.revHubBack.entity.PostVisibility.PUBLIC);
            }
        }

        return postRepository.save(post);
    }

    public void deletePost(Long id, String username) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getAuthor().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized to delete this post");
        }

        postRepository.delete(post);
    }

    public List<Post> getPostsByUser(String username, String currentUsername) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Post> userPosts = postRepository.findByAuthorOrderByCreatedDateDesc(user);
        
        // If viewing own posts, return all posts regardless of visibility
        if (currentUsername != null && currentUsername.equals(username)) {
            return userPosts;
        }
        
        // For other users, filter based on visibility and following status
        User currentUser = currentUsername != null ? userRepository.findByUsername(currentUsername).orElse(null) : null;
        
        final boolean isFollowing;
        if (currentUser != null) {
            List<User> following = followRepository.findFollowing(currentUser);
            isFollowing = following.stream().anyMatch(u -> u.getId().equals(user.getId()));
        } else {
            isFollowing = false;
        }
        
        return userPosts.stream()
            .filter(post -> post.getVisibility() == com.example.revHubBack.entity.PostVisibility.PUBLIC || 
                           (currentUser != null && isFollowing))
            .collect(java.util.stream.Collectors.toList());
    }
    
    public Post savePost(Post post) {
        return postRepository.save(post);
    }

    public Map<String, Object> toggleLike(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isLiked = post.getLikes().stream()
                .anyMatch(like -> like.getUser().getId().equals(user.getId()));

        if (isLiked) {
            post.getLikes().removeIf(like -> like.getUser().getId().equals(user.getId()));
            post.setLikesCount(post.getLikesCount() - 1);
        } else {
            Like like = new Like();
            like.setPost(post);
            like.setUser(user);
            post.getLikes().add(like);
            post.setLikesCount(post.getLikesCount() + 1);
            
            notificationService.createLikeNotification(post.getAuthor(), user, postId);
        }

        postRepository.save(post);
        
        Map<String, Object> response = new HashMap<>();
        response.put("likesCount", post.getLikesCount());
        response.put("isLiked", !isLiked);
        return response;
    }

    public Comment addComment(Long postId, String content, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setPost(post);
        comment.setAuthor(user);
        
        Comment savedComment = commentRepository.save(comment);
        
        post.setCommentsCount(post.getCommentsCount() + 1);
        postRepository.save(post);
        
        return savedComment;
    }

    public List<Comment> getComments(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return commentRepository.findByPostAndParentCommentIsNullOrderByCreatedDateDesc(post);
    }

    public void deleteComment(Long postId, Long commentId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getAuthor().getId().equals(user.getId()) && !post.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to delete this comment");
        }

        commentRepository.delete(comment);
        
        post.setCommentsCount(Math.max(0, post.getCommentsCount() - 1));
        postRepository.save(post);
    }
    
    public List<Post> searchPosts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new java.util.ArrayList<>();
        }
        
        String searchTerm = query.trim().toLowerCase();
        List<Post> allPosts = postRepository.findAllByOrderByCreatedDateDesc();
        
        return allPosts.stream()
            .filter(post -> {
                String content = post.getContent().toLowerCase();
                String authorUsername = post.getAuthor().getUsername().toLowerCase();
                
                return content.contains(searchTerm) || 
                       authorUsername.contains(searchTerm) ||
                       (searchTerm.startsWith("#") && content.contains(searchTerm)) ||
                       (!searchTerm.startsWith("#") && content.contains("#" + searchTerm));
            })
            .collect(java.util.stream.Collectors.toList());
    }
    
    private void processMentions(Post post, User author) {
        String content = post.getContent();
        System.out.println("Processing mentions - Content: " + content);
        
        if (content == null || content.trim().isEmpty()) {
            System.out.println("Content is null or empty, skipping mentions");
            return;
        }
        
        // Updated regex pattern to properly match @username
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("@([a-zA-Z0-9_]+)");
        java.util.regex.Matcher matcher = pattern.matcher(content);
        
        java.util.Set<String> processedUsers = new java.util.HashSet<>();
        boolean foundMentions = false;
        
        while (matcher.find()) {
            foundMentions = true;
            String mentionedUsername = matcher.group(1);
            System.out.println("Found mention: @" + mentionedUsername);
            
            // Avoid duplicate notifications for the same user
            if (processedUsers.contains(mentionedUsername)) {
                System.out.println("Skipping duplicate mention for: " + mentionedUsername);
                continue;
            }
            processedUsers.add(mentionedUsername);
            
            try {
                User mentionedUser = userRepository.findByUsername(mentionedUsername).orElse(null);
                if (mentionedUser != null) {
                    System.out.println("Found user: " + mentionedUser.getUsername() + " (ID: " + mentionedUser.getId() + ")");
                    if (!mentionedUser.getId().equals(author.getId())) {
                        System.out.println("Creating mention notification for: " + mentionedUsername);
                        // Create notification in both MongoDB and SQL
                        notificationService.createMentionNotification(mentionedUser, author, post.getId(), content);
                        notificationServiceSQL.createMentionNotification(mentionedUser, author, post.getId());
                        System.out.println("Mention notification created successfully");
                    } else {
                        System.out.println("Skipping self-mention for: " + mentionedUsername);
                    }
                } else {
                    System.out.println("User not found: " + mentionedUsername);
                }
            } catch (Exception e) {
                System.err.println("Error processing mention for user: " + mentionedUsername + ", Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        if (!foundMentions) {
            System.out.println("No mentions found in content: " + content);
        }
    }
}