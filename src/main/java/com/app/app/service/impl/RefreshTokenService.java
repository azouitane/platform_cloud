package com.app.app.service.impl;


import com.app.app.entity.RefreshToken;
import com.app.app.entity.User;
import com.app.app.exception.ValidationException;
import com.app.app.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository repository;

    // ── Generate secure token ──
    private String generateTokenValue() {
        byte[] bytes = new byte[64];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    // ── Create ──
    public RefreshToken create(User user) {

        long expirationMs = 7L * 24 * 60 * 60 * 1000;

        RefreshToken token = RefreshToken.builder()
                .token(generateTokenValue())
                .user(user)
                .expiryDate(Instant.now().plusMillis(expirationMs))
                .revoked(false)
                .rotated(false)
                .build();

        return repository.save(token);
    }

    // ── Verify ──
    public RefreshToken verify(String tokenValue) {

        RefreshToken token = repository.findByToken(tokenValue)
                .orElseThrow(() -> new ValidationException("Refresh token not found"));

        if (token.isRevoked() || token.isRotated()) {
            throw new ValidationException("Refresh token invalid");
        }

        if (token.getExpiryDate().isBefore(Instant.now())) {
            throw new ValidationException("Refresh token expired");
        }

        return token;
    }


    // ── Rotation ──
    public RefreshToken rotate(RefreshToken oldToken) {

        RefreshToken newToken = create(oldToken.getUser());

        oldToken.setRevoked(true);
        oldToken.setRotated(true);

        repository.save(oldToken);

        return newToken;
    }

    // ── Revoke all user tokens ──
    public void revokeAll(UUID UserId) {
        repository.deleteByUser_Id(UserId);
    }
}
