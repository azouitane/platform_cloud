package com.platformcloud.infrastructure.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateNodeRequest(
        @NotBlank(message = "Node name is required")
        @Size(max = 100)
        String name
) {}