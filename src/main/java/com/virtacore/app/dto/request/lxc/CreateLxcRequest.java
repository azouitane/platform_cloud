package com.virtacore.app.dto.request.lxc;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateLxcRequest(

        @NotBlank
        String hostname,

        @NotNull
        UUID templateId,

        @NotNull(message = "Cluster ID is required")
        UUID clusterId,

        @NotNull
        Integer cores,

        @NotNull
        Integer memory,

        @NotNull
        Integer swap,

        @NotNull
        Integer disk,

        String password,

        Boolean nesting
) {
}
