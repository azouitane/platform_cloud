package com.virtacore.app.dto.request.vm;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ClusterRequest(

        @NotBlank(message = "Cluster name is required")
        String name,

        @NotBlank(message = "Host is required")
        String host,

        @NotNull(message = "API port is required")
        @Min(value = 1, message = "API port must be greater than 0")
        Integer apiPort,

        @NotBlank(message = "Token ID is required")
        String tokenId,

        @NotBlank(message = "Token secret is required")
        String tokenSecret
) {
}
