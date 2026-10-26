package com.platformcloud.infrastructure.infrastructure.proxmox;

import com.platformcloud.infrastructure.application.port.ProxmoxGateway;
import com.platformcloud.infrastructure.application.port.SecretManager;
import com.platformcloud.infrastructure.domain.exception.ValidationException;
import com.platformcloud.infrastructure.domain.model.Cluster;
import fr.freshperf.pve4j.Proxmox;
import fr.freshperf.pve4j.SecurityConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProxmoxApiClient implements ProxmoxGateway {

    private final SecretManager secretManager;

    private Proxmox connect(Cluster cluster) {
        if (!cluster.enabled()) {
            throw new ValidationException("Proxmox cluster is disabled: " + cluster.name());
        }

        var credentials = secretManager.getProxmoxCredentials(
                cluster.secretRef()
        );

        return Proxmox.create(
                cluster.host(),
                cluster.port(),
                credentials.authorizationToken(),
                SecurityConfig.secure()
        );
    }

    @Override
    public boolean verifyConnection(Cluster cluster) {
        // Implement a read-only Proxmox API call here,
        // using the PVE4J version installed in the project.
        try {
            Proxmox client = connect(cluster);
            return true;

        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }
}