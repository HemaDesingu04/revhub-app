package com.example.revHubBack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PostRequest {
    @NotBlank
    @Size(max = 1000)
    private String content;
    
    private String imageUrl;
    
    private String mediaType;
    
    private String visibility;
}