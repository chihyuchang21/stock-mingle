package com.example.demo.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String accountName;
    private String password;
}
