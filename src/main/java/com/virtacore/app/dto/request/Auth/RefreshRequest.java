package com.virtacore.app.dto.request.Auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank(message = "refresh token is required")
        String refreshToken
) {
}
