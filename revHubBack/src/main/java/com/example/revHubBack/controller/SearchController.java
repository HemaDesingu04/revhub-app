package com.example.revHubBack.controller;

import com.example.revHubBack.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/search")
public class SearchController {
    @Autowired
    private SearchService searchService;

    @GetMapping
    public ResponseEntity<?> search(@RequestParam String q) {
        try {
            return ResponseEntity.ok(searchService.search(q));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Search failed: " + e.getMessage());
        }
    }
    
    @GetMapping("/hashtag")
    public ResponseEntity<?> searchByHashtag(@RequestParam String tag) {
        try {
            return ResponseEntity.ok(searchService.searchByHashtag(tag));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Hashtag search failed: " + e.getMessage());
        }
    }
    
    @GetMapping("/users")
    public ResponseEntity<?> searchUsers(@RequestParam(required = false) String q) {
        try {
            return ResponseEntity.ok(searchService.searchUsers(q));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("User search failed: " + e.getMessage());
        }
    }
}