package com.platformcloud.infrastructure.domain.model;

import com.platformcloud.infrastructure.domain.exception.ValidationException;

import java.time.Instant;
import java.util.UUID;

public record Cluster(
        UUID id,
        String name,
        String apiUrl,
        String secretRef,
        boolean enabled,
        Instant createdAt
) {
    public Cluster {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Cluster name is required");
        }

        if (apiUrl == null || apiUrl.isBlank()) {
            throw new ValidationException("Proxmox API URL is required");
        }
        if (secretRef == null || secretRef.isBlank()) {
            throw new ValidationException("Secret reference is required");
        }
    }
}
