package com.example.revHubBack.service;

import com.example.revHubBack.dto.CommentRequest;
import com.example.revHubBack.entity.Comment;
import com.example.revHubBack.entity.Post;
import com.example.revHubBack.entity.User;
import com.example.revHubBack.repository.CommentRepository;
import com.example.revHubBack.repository.PostRepository;
import com.example.revHubBack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationMongoService notificationService;

    public List<Comment> getCommentsByPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return commentRepository.findByPostAndParentCommentIsNullOrderByCreatedDateDesc(post);
    }

    @Transactional
    public Comment addReply(Long commentId, CommentRequest commentRequest, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        Comment reply = new Comment();
        reply.setContent(commentRequest.getContent());
        reply.setAuthor(user);
        reply.setPost(parentComment.getPost());
        reply.setParentComment(parentComment);

        return commentRepository.save(reply);
    }

    @Transactional
    public Comment addComment(Long postId, CommentRequest commentRequest, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = new Comment();
        comment.setContent(commentRequest.getContent());
        comment.setAuthor(user);
        comment.setPost(post);

        Comment savedComment = commentRepository.save(comment);
        
        post.setCommentsCount(post.getCommentsCount() + 1);
        postRepository.save(post);
        
        // Create notification for comment
        if (!post.getAuthor().getId().equals(user.getId())) {
            notificationService.createCommentNotification(post.getAuthor(), user, postId, commentRequest.getContent());
        }

        return savedComment;
    }

    @Transactional
    public void deleteComment(Long postId, Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        
        if (!comment.getAuthor().getUsername().equals(username) && 
            !post.getAuthor().getUsername().equals(username)) {
            throw new RuntimeException("Not authorized to delete this comment");
        }
        
        commentRepository.delete(comment);
        
        post.setCommentsCount(Math.max(0, post.getCommentsCount() - 1));
        postRepository.save(post);
    }
}