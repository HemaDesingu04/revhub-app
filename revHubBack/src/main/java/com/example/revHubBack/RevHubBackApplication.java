package com.example.revHubBack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.revHubBack.repository")
@EnableMongoRepositories(basePackages = "com.example.revHubBack.repository")
public class RevHubBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(RevHubBackApplication.class, args);
	}

}
