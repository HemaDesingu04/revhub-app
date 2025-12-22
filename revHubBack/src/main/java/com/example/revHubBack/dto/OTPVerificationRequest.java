package com.example.revHubBack.dto;

import lombok.Data;

@Data
public class OTPVerificationRequest {
    private String email;
    private String otp;
}