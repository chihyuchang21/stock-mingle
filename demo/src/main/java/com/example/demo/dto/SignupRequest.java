package com.example.demo.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class SignupRequest {
    private String accountName;
    private List<String> hashtags;
    private MultipartFile image;
}
