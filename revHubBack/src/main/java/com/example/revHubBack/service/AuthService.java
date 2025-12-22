package com.example.revHubBack.service;

import com.example.revHubBack.dto.JwtResponse;
import com.example.revHubBack.dto.LoginRequest;
import com.example.revHubBack.dto.RegisterRequest;
import com.example.revHubBack.entity.User;
import com.example.revHubBack.repository.UserRepository;
import com.example.revHubBack.security.JwtUtils;
import com.example.revHubBack.security.UserPrincipal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;
    


    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();
        return new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getUsername());
    }

    @Autowired
    private EmailVerificationService emailVerificationService;
    
    public String registerUser(RegisterRequest signUpRequest) {
        try {
            if (userRepository.existsByUsername(signUpRequest.getUsername())) {
                return "Error: Username is already taken!";
            }

            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                return "Error: Email is already in use!";
            }

            User user = new User();
            user.setUsername(signUpRequest.getUsername());
            user.setEmail(signUpRequest.getEmail());
            user.setPassword(encoder.encode(signUpRequest.getPassword()));
            user.setIsVerified(false);

            userRepository.save(user);
            
            // Send OTP verification email
            emailVerificationService.sendVerificationOTP(user.getEmail());
            
            return "User registered successfully! Please check your email for verification OTP.";
        } catch (Exception e) {
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }
    

}