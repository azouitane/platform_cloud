package com.virtacore.app.service;


import com.virtacore.app.dto.request.Auth.AuthResponse;
import com.virtacore.app.dto.request.Auth.LoginRequest;
import com.virtacore.app.dto.request.Auth.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest registerRequest);
    AuthResponse login(LoginRequest loginRequest);
}
