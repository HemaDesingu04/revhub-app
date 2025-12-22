package com.example.revHubBack.controller;

import com.example.revHubBack.entity.Follow;
import com.example.revHubBack.entity.Post;
import com.example.revHubBack.entity.User;
import com.example.revHubBack.repository.UserRepository;
import com.example.revHubBack.repository.FollowRepository;
import com.example.revHubBack.service.FollowService;
import com.example.revHubBack.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/profile")
public class ProfileController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostService postService;
    
    @Autowired
    private FollowRepository followRepository;
    
    @Autowired
    private FollowService followService;

    @GetMapping("/{username}")
    public ResponseEntity<Map<String, Object>> getProfile(@PathVariable String username) {
        return userRepository.findByUsername(username)
                .map(user -> {
                    Map<String, Object> profile = new HashMap<>();
                    profile.put("id", user.getId());
                    profile.put("username", user.getUsername());
                    profile.put("email", user.getEmail());
                    profile.put("profilePicture", user.getProfilePicture());
                    profile.put("bio", user.getBio());
                    profile.put("isPrivate", user.getIsPrivate());
                    profile.put("createdDate", user.getCreatedDate());
                    profile.put("followersCount", followRepository.countFollowers(user));
                    profile.put("followingCount", followRepository.countFollowing(user));
                    return ResponseEntity.ok(profile);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{username}/posts")
    public ResponseEntity<List<Post>> getUserPosts(@PathVariable String username, Authentication authentication) {
        try {
            String currentUsername = authentication != null ? authentication.getName() : null;
            List<Post> posts = postService.getPostsByUser(username, currentUsername);
            return ResponseEntity.ok(posts);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping
    public ResponseEntity<User> updateProfile(@RequestBody Map<String, String> updates, Authentication authentication) {
        try {
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (updates.containsKey("bio")) {
                user.setBio(updates.get("bio"));
            }
            if (updates.containsKey("profilePicture")) {
                String profilePicture = updates.get("profilePicture");
                if (profilePicture != null && profilePicture.length() > 0) {
                    user.setProfilePicture(profilePicture);
                }
            }
            if (updates.containsKey("isPrivate")) {
                user.setIsPrivate(Boolean.parseBoolean(updates.get("isPrivate")));
            }

            User updatedUser = userRepository.save(user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<User> updateProfileWithFile(
            @RequestParam(value = "bio", required = false) String bio,
            @RequestParam(value = "profilePicture", required = false) org.springframework.web.multipart.MultipartFile profilePicture,
            @RequestParam(value = "isPrivate", required = false) String isPrivate,
            Authentication authentication) {
        try {
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (bio != null) {
                user.setBio(bio);
            }
            
            if (profilePicture != null && !profilePicture.isEmpty()) {
                byte[] fileBytes = profilePicture.getBytes();
                String base64File = java.util.Base64.getEncoder().encodeToString(fileBytes);
                String mimeType = profilePicture.getContentType();
                String dataUrl = "data:" + mimeType + ";base64," + base64File;
                user.setProfilePicture(dataUrl);
            }
            
            if (isPrivate != null) {
                user.setIsPrivate(Boolean.parseBoolean(isPrivate));
            }

            User updatedUser = userRepository.save(user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            throw new RuntimeException("Error updating profile: " + e.getMessage());
        }
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }
    
    @PostMapping("/follow/{username}")
    public ResponseEntity<Map<String, String>> followUser(@PathVariable String username, Authentication authentication) {
        try {
            String message = followService.followUser(authentication.getName(), username);
            Map<String, String> response = new HashMap<>();
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @DeleteMapping("/unfollow/{username}")
    public ResponseEntity<Map<String, String>> unfollowUser(@PathVariable String username, Authentication authentication) {
        try {
            followService.unfollowUser(authentication.getName(), username);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Unfollowed successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/follow-requests")
    public ResponseEntity<List<Follow>> getPendingFollowRequests(Authentication authentication) {
        try {
            List<Follow> requests = followService.getPendingFollowRequests(authentication.getName());
            return ResponseEntity.ok(requests);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/follow-requests/{followId}/accept")
    public ResponseEntity<Map<String, String>> acceptFollowRequest(@PathVariable Long followId, Authentication authentication) {
        try {
            followService.acceptFollowRequest(authentication.getName(), followId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Follow request accepted");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/follow-requests/{followId}/reject")
    public ResponseEntity<Map<String, String>> rejectFollowRequest(@PathVariable Long followId, Authentication authentication) {
        try {
            followService.rejectFollowRequest(authentication.getName(), followId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Follow request rejected");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/follow-status/{username}")
    public ResponseEntity<Map<String, String>> getFollowStatus(@PathVariable String username, Authentication authentication) {
        try {
            String status = followService.getFollowStatus(authentication.getName(), username);
            Map<String, String> response = new HashMap<>();
            response.put("status", status);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/{username}/followers")
    public ResponseEntity<List<User>> getFollowers(@PathVariable String username) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            List<User> followers = followRepository.findFollowers(user);
            return ResponseEntity.ok(followers);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{username}/following")
    public ResponseEntity<List<User>> getFollowing(@PathVariable String username) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            List<User> following = followRepository.findFollowing(user);
            return ResponseEntity.ok(following);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/cancel-request/{username}")
    public ResponseEntity<Map<String, String>> cancelFollowRequest(@PathVariable String username, Authentication authentication) {
        try {
            followService.cancelFollowRequest(authentication.getName(), username);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Follow request cancelled");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @DeleteMapping("/remove-follower/{username}")
    public ResponseEntity<Map<String, String>> removeFollower(@PathVariable String username, Authentication authentication) {
        try {
            followService.removeFollower(authentication.getName(), username);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Follower removed successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String query) {
        try {
            List<User> users = userRepository.findByUsernameContainingIgnoreCase(query);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

}