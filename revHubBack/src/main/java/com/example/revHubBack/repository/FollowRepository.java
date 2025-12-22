package com.example.revHubBack.repository;

import com.example.revHubBack.entity.Follow;
import com.example.revHubBack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowerAndFollowing(User follower, User following);
    
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);
    
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.following = :user AND f.status = 'ACCEPTED'")
    long countFollowers(User user);
    
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.follower = :user AND f.status = 'ACCEPTED'")
    long countFollowing(User user);
    
    @Query("SELECT f.follower FROM Follow f WHERE f.following = :user AND f.status = 'ACCEPTED'")
    List<User> findFollowers(User user);
    
    @Query("SELECT f.following FROM Follow f WHERE f.follower = :user AND f.status = 'ACCEPTED'")
    List<User> findFollowing(User user);
    
    @Query("SELECT f FROM Follow f WHERE f.following = :user AND f.status = 'PENDING'")
    List<Follow> findPendingFollowRequests(User user);
}