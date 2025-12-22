package com.example.revHubBack.service;

import com.example.revHubBack.entity.User;
import com.example.revHubBack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;

@Service
public class EmailVerificationService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JavaMailSender mailSender;
    
    private final ConcurrentHashMap<String, OTPData> otpStorage = new ConcurrentHashMap<>();
    
    private static class OTPData {
        String otp;
        LocalDateTime expiry;
        
        OTPData(String otp, LocalDateTime expiry) {
            this.otp = otp;
            this.expiry = expiry;
        }
    }
    
    public String sendVerificationOTP(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return "User not found";
        }
        
        if (user.getIsVerified()) {
            return "Email already verified";
        }
        
        String otp = String.format("%06d", new Random().nextInt(999999));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);
        
        otpStorage.put(email, new OTPData(otp, expiry));
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setFrom("doddakarthikreddy11122@gmail.com");
        message.setSubject("Email Verification OTP - RevHub");
        message.setText("Hello,\n\nWelcome to RevHub!\n\nYour verification OTP is: " + otp + "\n\nThis OTP will expire in 10 minutes.\n\nPlease enter this OTP to verify your email address.\n\nBest regards,\nRevHub Team");
        
        try {
            mailSender.send(message);
            return "Verification OTP sent successfully";
        } catch (Exception e) {
            return "Failed to send verification OTP: " + e.getMessage();
        }
    }
    
    public String verifyOTP(String email, String otp) {
        OTPData otpData = otpStorage.get(email);
        if (otpData == null) {
            return "No OTP found for this email";
        }
        
        if (LocalDateTime.now().isAfter(otpData.expiry)) {
            otpStorage.remove(email);
            return "OTP has expired";
        }
        
        if (!otpData.otp.equals(otp)) {
            return "Invalid OTP";
        }
        
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return "User not found";
        }
        
        user.setIsVerified(true);
        userRepository.save(user);
        otpStorage.remove(email);
        
        return "Email verified successfully";
    }
    
    public String sendVerificationEmail(String email) {
        return sendVerificationOTP(email);
    }

    public String verifyEmail(String token) {
        User user = userRepository.findAll().stream()
                .filter(u -> token.equals(u.getVerificationToken()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));
        
        user.setIsVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);
        
        return "Email verified successfully";
    }
}