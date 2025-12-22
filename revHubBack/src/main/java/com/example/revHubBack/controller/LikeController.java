package com.example.revHubBack.controller;

import com.example.revHubBack.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
public class LikeController {
    @Autowired
    private LikeService likeService;

    @PostMapping("/{id}/toggle-like")
    public ResponseEntity<?> toggleLike(@PathVariable Long id, Authentication authentication) {
        try {
            return ResponseEntity.ok(likeService.toggleLike(id, authentication.getName()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}