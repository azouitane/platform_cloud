package com.virtacore.app.dto.request.lxc;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LxcTemplateRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotNull(message = "Cluster ID is required")
        UUID clusterId,

        @NotBlank(message = "Ostemplate is required")
        String ostemplate,

        @NotBlank(message = "Distribution is required")
        String distribution,

        @NotBlank(message = "Version is required")
        String version,

        @NotBlank(message = "Format is required")
        String format,

        @NotBlank(message = "Storage is required")
        String storage,

        @NotNull(message = "Available flag is required")
        Boolean available
) {
}
