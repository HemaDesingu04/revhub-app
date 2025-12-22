package com.example.revHubBack.service;

import com.example.revHubBack.entity.PasswordResetToken;
import com.example.revHubBack.entity.User;
import com.example.revHubBack.repository.PasswordResetTokenRepository;
import com.example.revHubBack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {
    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EmailService emailService;
    
    public String createPasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user);
        tokenRepository.save(resetToken);
        
        try {
            emailService.sendPasswordResetEmail(email, token);
            System.out.println("Email sent successfully to: " + email);
            return "Password reset email sent successfully!";
        } catch (Exception e) {
            System.out.println("Email failed: " + e.getMessage() + ", Token: " + token);
            e.printStackTrace();
            return "Email failed: " + e.getMessage() + ". Token: " + token;
        }
    }

    @Transactional
    public String resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        
        if (resetToken.getUsed() || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired or already used");
        }
        
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
        
        return "Password reset successfully";
    }
}