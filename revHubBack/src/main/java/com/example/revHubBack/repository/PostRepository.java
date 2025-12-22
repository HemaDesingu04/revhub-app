package com.example.revHubBack.repository;

import com.example.revHubBack.entity.Post;
import com.example.revHubBack.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.repository.query.Param;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findAllByOrderByCreatedDateDesc(Pageable pageable);
    List<Post> findAllByOrderByCreatedDateDesc();
    
    List<Post> findByAuthorOrderByCreatedDateDesc(User author);
    List<Post> findByAuthorAndCreatedDateAfterOrderByCreatedDateDesc(User author, LocalDateTime date);
    List<Post> findByCreatedDateAfterOrderByCreatedDateDesc(LocalDateTime date);
    
    @Query("SELECT p FROM Post p WHERE p.visibility = 'PUBLIC' ORDER BY p.createdDate DESC")
    Page<Post> findPublicPosts(Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE (p.author.id = :userId) OR (p.author.id IN :followingIds) ORDER BY p.createdDate DESC")
    Page<Post> findFollowersPosts(@Param("userId") Long userId, @Param("followingIds") List<Long> followingIds, Pageable pageable);
}