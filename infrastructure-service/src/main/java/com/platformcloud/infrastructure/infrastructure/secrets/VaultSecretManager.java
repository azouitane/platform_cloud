package com.platformcloud.infrastructure.infrastructure.secrets;

import com.platformcloud.infrastructure.application.port.SecretManager;
import com.platformcloud.infrastructure.domain.model.ProxmoxCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class VaultSecretManager implements SecretManager {

    private final RestClient client;

    public VaultSecretManager(
            RestClient.Builder builder,
            @Value("${vault.url}") String vaultUrl,
            @Value("${vault.token}") String vaultToken
    ) {
        this.client = builder
                .baseUrl(vaultUrl)
                .defaultHeader("X-Vault-Token", vaultToken)
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public ProxmoxCredentials getCredentials(String secretRef) {

        if (secretRef == null ||
                !secretRef.matches("proxmox/clusters/[a-zA-Z0-9_-]+")) {
            throw new IllegalArgumentException(
                    "Invalid secret reference"
            );
        }

        Map<String, Object> response = client.get()
                .uri("/v1/secret/data/{path}", secretRef)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new IllegalStateException("Vault returned no data");
        }

        Map<String, Object> outer =
                (Map<String, Object>) response.get("data");

        if (outer == null) {
            throw new IllegalStateException("Vault data missing");
        }

        Map<String, Object> data =
                (Map<String, Object>) outer.get("data");

        if (data == null) {
            throw new IllegalStateException("Vault secret missing");
        }

        return new ProxmoxCredentials(
                (String) data.get("token_id"),
                (String) data.get("token_secret")
        );
    }
}