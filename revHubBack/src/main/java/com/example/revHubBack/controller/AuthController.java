package com.example.revHubBack.controller;

import com.example.revHubBack.dto.JwtResponse;
import com.example.revHubBack.dto.LoginRequest;
import com.example.revHubBack.dto.RegisterRequest;
import com.example.revHubBack.service.AuthService;
import com.example.revHubBack.service.PasswordResetService;
import com.example.revHubBack.service.EmailVerificationService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok(jwtResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: Invalid credentials!");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest signUpRequest) {
        try {
            String result = authService.registerUser(signUpRequest);
            if (result.startsWith("Error")) {
                return ResponseEntity.badRequest().body(result);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        return ResponseEntity.ok("User logged out successfully!");
    }
    
    @Autowired
    private PasswordResetService passwordResetService;
    
    @Autowired
    private EmailVerificationService emailVerificationService;
    

    
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody com.example.revHubBack.dto.PasswordResetRequest request) {
        try {
            String result = passwordResetService.createPasswordResetToken(request.getEmail());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody com.example.revHubBack.dto.PasswordResetConfirmRequest request) {
        try {
            String result = passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @PostMapping("/send-verification")
    public ResponseEntity<?> sendVerification(@RequestParam String email) {
        try {
            String result = emailVerificationService.sendVerificationEmail(email);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            String result = emailVerificationService.verifyEmail(token);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@RequestBody com.example.revHubBack.dto.OTPVerificationRequest request) {
        try {
            String result = emailVerificationService.verifyOTP(request.getEmail(), request.getOtp());
            if (result.equals("Email verified successfully")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    

}