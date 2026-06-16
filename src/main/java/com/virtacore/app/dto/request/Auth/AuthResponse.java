package com.virtacore.app.dto.request.Auth;

import java.time.LocalDateTime;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        LocalDateTime expiresAt
) {
}
