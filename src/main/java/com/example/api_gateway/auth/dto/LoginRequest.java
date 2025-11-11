package com.example.api_gateway.auth.dto;

import lombok.Data;

@Data
public class LoginRequest {
    String username;
    String password;
}
