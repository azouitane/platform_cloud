
package com.platformcloud.infrastructure.domain.model;

import com.platformcloud.infrastructure.domain.exception.ValidationException;

public record ProxmoxCredentials(
        String tokenId,
        String tokenSecret
) {
    public ProxmoxCredentials {
        if (tokenId == null || tokenId.isBlank()) {
            throw new ValidationException(
                    "Proxmox API Token ID is required"
            );
        }

        if (tokenSecret == null || tokenSecret.isBlank()) {
            throw new ValidationException(
                    "Proxmox API Token Secret is required"
            );
        }
    }

    /**
     * Authentication format required by Proxmox:
     * USER@REALM!TOKENID=SECRET
     */
    public String authorizationToken() {
        return tokenId + "=" + tokenSecret;
    }

    @Override
    public String toString() {
        return "ProxmoxCredentials[REDACTED]";
    }
}
