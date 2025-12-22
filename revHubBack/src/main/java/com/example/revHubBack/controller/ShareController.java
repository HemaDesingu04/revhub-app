package com.example.revHubBack.controller;

import com.example.revHubBack.service.ShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
public class ShareController {
    @Autowired
    private ShareService shareService;

    @PostMapping("/{id}/share")
    public ResponseEntity<?> sharePost(@PathVariable Long id, Authentication authentication) {
        try {
            String result = shareService.sharePost(id, authentication.getName());
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}