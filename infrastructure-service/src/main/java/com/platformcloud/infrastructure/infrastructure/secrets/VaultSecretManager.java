
package com.platformcloud.infrastructure.infrastructure.secrets;

import com.platformcloud.infrastructure.application.port.SecretManager;
import com.platformcloud.infrastructure.domain.model.ProxmoxCredentials;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.vault.core.VaultKeyValueOperations;
import org.springframework.vault.core.VaultKeyValueOperationsSupport;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.VaultResponseSupport;

import java.util.Map;

@Slf4j
@Component
public class VaultSecretManager implements SecretManager {

    private final VaultKeyValueOperations kv;

    public VaultSecretManager(
            VaultOperations vaultOperations,
            @Value("${platformcloud.secrets.proxmox-mount:secret}")
            String mount
    ) {
        this.kv = vaultOperations.opsForKeyValue(
                mount,
                VaultKeyValueOperationsSupport.KeyValueBackend.KV_2
        );
    }

    @Override
    public ProxmoxCredentials getProxmoxCredentials(
            String secretRef
    ) {
        if (secretRef == null ||
                !secretRef.matches("proxmox/[a-zA-Z0-9_-]+")) {

            throw new IllegalArgumentException(
                    "Invalid Proxmox secret reference"
            );
        }

        VaultResponseSupport<Map> response =
                kv.get(secretRef, Map.class);

        if (response == null || response.getData() == null) {
            throw new IllegalStateException(
                    "Proxmox credentials not found"
            );
        }

        Map<?, ?> data = response.getData();

        return new ProxmoxCredentials(
                requiredString(data, "token_id"),
                requiredString(data, "token_secret")
        );
    }

    private String requiredString(
            Map<?, ?> data,
            String key
    ) {
        Object value = data.get(key);

        if (!(value instanceof String text) || text.isBlank()) {
            throw new IllegalStateException(
                    "Missing Proxmox credential field: " + key
            );
        }

        return text;
    }
}
