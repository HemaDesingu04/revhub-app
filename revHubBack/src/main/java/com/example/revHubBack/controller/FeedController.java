package com.example.revHubBack.controller;

import com.example.revHubBack.service.FeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/feed")
public class FeedController {
    @Autowired
    private FeedService feedService;

    @GetMapping
    public ResponseEntity<?> getPersonalizedFeed(Authentication authentication, Pageable pageable) {
        try {
            String username = authentication.getName();
            return ResponseEntity.ok(feedService.getPersonalizedFeed(username, pageable));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to load feed: " + e.getMessage());
        }
    }
}