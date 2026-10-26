package com.platformcloud.infrastructure.domain.model;

import com.platformcloud.infrastructure.domain.exception.ValidationException;

import java.time.Instant;
import java.util.UUID;

public record Cluster(
        UUID id,
        String name,
        String host,
        int port,
        String secretRef,
        boolean enabled,
        Instant createdAt
) {
    public String apiUrl() {
        return "https://" + host + ":" + port;
    }
}