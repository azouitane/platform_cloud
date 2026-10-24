package com.platformcloud.infrastructure.application.port;

import com.platformcloud.infrastructure.domain.model.ProxmoxCredentials;

public interface SecretManager {
    ProxmoxCredentials getCredentials(String secretRef);
}