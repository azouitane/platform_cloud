package com.platformcloud.infrastructure.presentation.response;

import java.time.Instant;
import java.util.UUID;

public record ClusterResponse(
        UUID id,
        String name,
        String host,
        int port,
        boolean enabled,
        Instant createdAt
) {
}