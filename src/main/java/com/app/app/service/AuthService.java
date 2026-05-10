package com.app.app.service;


import com.app.app.dto.request.Auth.AuthResponse;
import com.app.app.dto.request.Auth.LoginRequest;
import com.app.app.dto.request.Auth.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest registerRequest);
    AuthResponse login(LoginRequest loginRequest);
}
