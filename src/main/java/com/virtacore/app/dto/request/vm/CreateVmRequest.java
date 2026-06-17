package com.virtacore.app.dto.request.vm;

import java.util.UUID;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record CreateVmRequest(

        @NotBlank(message = "VM name is required")
        String name,

        @NotNull(message = "Cluster ID is required")
        UUID clusterId,

        @NotNull(message = "Template ID is required")
        @Positive(message = "Template ID must be positive")
        Long templateId,

        @NotNull(message = "CPU is required")
        @Min(value = 1, message = "CPU must be at least 1")
        Integer cpu,

        @NotNull(message = "Memory is required")
        @Min(value = 256, message = "Memory must be at least 256 MB")
        Integer memory,

        @NotNull(message = "Disk size is required")
        @Min(value = 1, message = "Disk must be at least 1 GB")
        Integer disk,

        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must contain at least 8 characters")
        String password
) {}