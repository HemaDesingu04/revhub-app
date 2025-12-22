package com.example.revHubBack.repository;

import com.example.revHubBack.entity.NotificationMongo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationMongoRepository extends MongoRepository<NotificationMongo, String> {
    
    List<NotificationMongo> findByUserIdOrderByCreatedDateDesc(String userId);
    
    List<NotificationMongo> findByUserIdAndReadStatusFalse(String userId);
    
    long countByUserIdAndReadStatusFalse(String userId);
}