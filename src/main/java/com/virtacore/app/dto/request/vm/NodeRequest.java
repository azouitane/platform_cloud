package com.virtacore.app.dto.request.vm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record NodeRequest(

        @NotBlank(message = "Node name is required")
        String name,

        @NotBlank(message = "Node host is required")
        String host,

        @NotNull(message = "Cluster ID is required")
        UUID clusterId
) {
}
