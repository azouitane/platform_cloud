package com.virtacore.app.controller;

import com.virtacore.app.dto.request.Auth.AuthResponse;
import com.virtacore.app.dto.request.Auth.LoginRequest;
import com.virtacore.app.dto.request.Auth.RefreshRequest;
import com.virtacore.app.dto.request.Auth.RegisterRequest;
import com.virtacore.app.dto.request.common.ApiResponse;
import com.virtacore.app.security.CustomUserDetails;
import com.virtacore.app.service.impl.AuthServiceImp;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {


    private final AuthServiceImp authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created successfully.", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success("Login successful.", response)
        );
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest req) {
        return authService.refresh(req.refreshToken());
    }

    @PostMapping("/logout")
    public void logout(@RequestBody RefreshRequest req) {
        authService.logout(req.refreshToken());
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }


        return ResponseEntity.ok(authService.getCurrentUser(userDetails.getId()));
    }
}
