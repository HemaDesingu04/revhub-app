package com.example.revHubBack.repository;

import com.example.revHubBack.entity.Comment;
import com.example.revHubBack.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostAndParentCommentIsNullOrderByCreatedDateDesc(Post post);
    List<Comment> findByParentCommentOrderByCreatedDateAsc(Comment parentComment);
}