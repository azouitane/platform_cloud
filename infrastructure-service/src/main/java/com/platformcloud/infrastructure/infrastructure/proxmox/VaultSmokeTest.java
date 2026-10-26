package com.platformcloud.infrastructure.infrastructure.proxmox;

import com.platformcloud.infrastructure.application.port.SecretManager;
import com.platformcloud.infrastructure.domain.model.ProxmoxCredentials;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("vault-smoke-test")
@RequiredArgsConstructor
public class VaultSmokeTest implements ApplicationRunner {

    private final SecretManager secretManager;

    @Override
    public void run(ApplicationArguments args) {
        ProxmoxCredentials credentials =
                secretManager.getProxmoxCredentials(
                        "proxmox/cluster-01"
                );

        System.out.println(
                "OpenBao integration successful: " +
                        !credentials.tokenId().isBlank()
        );
    }
}