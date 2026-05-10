package com.app.app.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
