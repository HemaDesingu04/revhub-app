package com.example.revHubBack.repository;

import com.example.revHubBack.entity.Like;
import com.example.revHubBack.entity.Post;
import com.example.revHubBack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUserAndPost(User user, Post post);
    boolean existsByUserAndPost(User user, Post post);
}