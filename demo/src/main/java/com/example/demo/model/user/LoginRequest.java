package com.example.demo.model.user;

import lombok.Data;

@Data
public class LoginRequest {
    private String accountName;
    private String password;
}
