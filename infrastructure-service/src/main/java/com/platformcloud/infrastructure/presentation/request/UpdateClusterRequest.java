package com.platformcloud.infrastructure.presentation.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateClusterRequest(

        @NotBlank(message = "Cluster name is required")
        @Size(max = 100)
        String name,

        @NotBlank(message = "Proxmox host is required")
        @Size(max = 255)
        String host,

        @NotBlank(message = "Secret reference is required")
        @Size(max = 255)
        String secretRef,

        boolean enabled

) {
}
