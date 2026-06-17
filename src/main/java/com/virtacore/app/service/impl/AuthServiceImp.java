package com.virtacore.app.service.impl;


import com.virtacore.app.Enums.UserRole;
import com.virtacore.app.dto.request.Auth.AuthResponse;
import com.virtacore.app.dto.request.Auth.LoginRequest;
import com.virtacore.app.dto.request.Auth.RegisterRequest;
import com.virtacore.app.dto.response.UserResponse;
import com.virtacore.app.entity.user.RefreshToken;
import com.virtacore.app.entity.user.User;
import com.virtacore.app.exception.ResourceNotFoundException;
import com.virtacore.app.exception.ValidationException;
import com.virtacore.app.mapper.UserMapper;
import com.virtacore.app.repository.UserRepository;
import com.virtacore.app.security.JwtService;
import com.virtacore.app.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImp implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Override
    public AuthResponse register(RegisterRequest request) {


        if (userRepository.existsByEmail(request.email().trim().toLowerCase())) {
            throw new ValidationException("Email already exists");
        }

        User user = userMapper.toEntity(request);

        user.setEmail(request.email().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.CUSTOMER);
        user.setIsActive(true);
        user = userRepository.save(user);

        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {


        String email = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ValidationException("Invalid email or password."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ValidationException("Invalid email or password.");
        }

        if (!user.getIsActive()) {
            throw new ValidationException("Your account has been suspended.");
        }

        return buildAuthResponse(user);
    }


    @Transactional
    public AuthResponse refresh(String refreshTokenValue) {

        RefreshToken oldToken = refreshTokenService.verify(refreshTokenValue);

        User user = oldToken.getUser();

        RefreshToken newRefresh = refreshTokenService.rotate(oldToken);

        String accessToken = jwtService.generateToken(user);

        return new AuthResponse(
                accessToken,
                newRefresh.getToken(),
                LocalDateTime.ofInstant(
                        Instant.now().plusMillis(jwtService.getExpiration()),
                        ZoneId.systemDefault()
                )
        );
    }

    @Transactional
    public void logout(String refreshTokenValue) {

        RefreshToken token = refreshTokenService.verify(refreshTokenValue);

        refreshTokenService.revokeAll(token.getId());
    }


    public UserResponse getCurrentUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return userMapper.toResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {

        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.create(user).getToken();

        return new AuthResponse(
                accessToken,
                refreshToken,
                LocalDateTime.ofInstant(
                        Instant.now().plusMillis(jwtService.getExpiration()),
                        ZoneId.systemDefault()
                )
        );
    }
}