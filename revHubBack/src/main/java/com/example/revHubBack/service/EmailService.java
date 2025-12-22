package com.example.revHubBack.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendPasswordResetEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom("doddakarthikreddy11122@gmail.com");
        message.setSubject("RevHub - Password Reset Request");
        message.setText("Hello,\n\nYou requested a password reset for your RevHub account.\n\n" +
                "Click the link below to reset your password:\n" +
                "http://localhost:4200/auth/reset-password?token=" + token + "\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "If you didn't request this, please ignore this email.\n\n" +
                "Best regards,\nRevHub Team");
        mailSender.send(message);
    }
}