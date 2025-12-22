package com.example.revHubBack.repository;

import com.example.revHubBack.entity.Share;
import com.example.revHubBack.entity.Post;
import com.example.revHubBack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShareRepository extends JpaRepository<Share, Long> {
    Optional<Share> findByUserAndPost(User user, Post post);
    boolean existsByUserAndPost(User user, Post post);
}