package com.example.revHubBack.controller;

import com.example.revHubBack.dto.CommentRequest;
import com.example.revHubBack.entity.Comment;
import com.example.revHubBack.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long id) {
        try {
            List<Comment> comments = commentService.getCommentsByPost(id);
            return ResponseEntity.ok(comments);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<Comment> addComment(@PathVariable Long id, 
                                            @Valid @RequestBody CommentRequest commentRequest,
                                            Authentication authentication) {
        try {
            Comment comment = commentService.addComment(id, commentRequest, authentication.getName());
            return ResponseEntity.ok(comment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/comments/{commentId}/replies")
    public ResponseEntity<Comment> addReply(@PathVariable Long commentId,
                                          @Valid @RequestBody CommentRequest commentRequest,
                                          Authentication authentication) {
        try {
            Comment reply = commentService.addReply(commentId, commentRequest, authentication.getName());
            return ResponseEntity.ok(reply);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long postId, 
                                            @PathVariable Long commentId,
                                            Authentication authentication) {
        try {
            commentService.deleteComment(postId, commentId, authentication.getName());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}