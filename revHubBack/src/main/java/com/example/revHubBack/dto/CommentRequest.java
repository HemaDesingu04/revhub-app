package com.example.revHubBack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentRequest {
    @NotBlank
    @Size(max = 500)
    private String content;
}