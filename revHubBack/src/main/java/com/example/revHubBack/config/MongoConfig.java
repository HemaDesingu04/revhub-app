package com.example.revHubBack.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {
    
    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;
    
    @Override
    protected String getDatabaseName() {
        return "revhubteam4";
    }
    
    @Override
    public MongoClient mongoClient() {
        return MongoClients.create(mongoUri);
    }
}