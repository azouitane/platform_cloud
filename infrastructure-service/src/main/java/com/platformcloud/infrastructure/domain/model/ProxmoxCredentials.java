package com.platformcloud.infrastructure.domain.model;

import com.platformcloud.infrastructure.domain.exception.ValidationException;

public record ProxmoxCredentials(
        String tokenId,
        String tokenSecret
) {
    public ProxmoxCredentials {
        if (tokenId == null || tokenId.isBlank()
                || tokenSecret == null || tokenSecret.isBlank()) {
            throw new ValidationException(
                    "Invalid Proxmox credentials"
            );
        }
    }
}