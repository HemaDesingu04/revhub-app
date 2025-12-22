package com.example.revHubBack.dto;

import lombok.Data;

@Data
public class PasswordResetConfirmRequest {
    private String token;
    private String newPassword;
}