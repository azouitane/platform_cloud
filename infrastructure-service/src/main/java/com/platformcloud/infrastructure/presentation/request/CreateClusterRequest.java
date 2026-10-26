
package com.platformcloud.infrastructure.presentation.request;

import jakarta.validation.constraints.*;

public record CreateClusterRequest(

        @NotBlank(message = "Cluster name is required")
        @Size(max = 100)
        String name,

        @NotBlank(message = "Proxmox host is required")
        @Size(max = 255)
        String host,

        @Min(value = 1, message = "Port must be greater than 0")
        @Max(value = 65535, message = "Port must not exceed 65535")
        @NotNull(message = "Proxmox port is required")
        Integer port,

        @NotBlank(message = "Secret reference is required")
        @Size(max = 255)
        String secretRef

) { }
