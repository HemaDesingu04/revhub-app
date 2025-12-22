package com.example.revHubBack.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/features")
public class FeatureController {

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getFeatureStatus() {
        Map<String, Object> features = new HashMap<>();
        
        features.put("passwordReset", Map.of(
            "implemented", true,
            "endpoints", new String[]{
                "POST /auth/forgot-password",
                "POST /auth/reset-password"
            }
        ));
        
        features.put("accountVerification", Map.of(
            "implemented", true,
            "endpoints", new String[]{
                "POST /auth/send-verification",
                "GET /auth/verify-email"
            }
        ));
        
        features.put("hashtagsAndMentions", Map.of(
            "implemented", true,
            "description", "Automatic processing in posts, search by hashtags available"
        ));
        
        features.put("postVisibility", Map.of(
            "implemented", true,
            "options", new String[]{"PUBLIC", "FOLLOWERS_ONLY"},
            "description", "Set visibility when creating posts"
        ));
        
        features.put("searchFunctionality", Map.of(
            "implemented", true,
            "endpoints", new String[]{
                "GET /search?q={query}",
                "GET /search/hashtag?tag={hashtag}"
            }
        ));
        
        features.put("newsFeedAlgorithm", Map.of(
            "implemented", true,
            "endpoint", "GET /feed",
            "description", "Personalized feed based on followed users + popular content"
        ));
        
        return ResponseEntity.ok(features);
    }
}