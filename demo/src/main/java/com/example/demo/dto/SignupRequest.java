package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class SignupRequest {
    private String accountName;
    private List<String> hashtags;
}
