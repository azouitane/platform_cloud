package com.virtacore.app.dto.request.lxc;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateLxcTemplateRequest(
        @NotBlank(message = "Name is required")
        String name,

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
