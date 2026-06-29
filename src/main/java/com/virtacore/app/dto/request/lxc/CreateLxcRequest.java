package com.virtacore.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateLxcRequest(
        @NotNull
        Long vmId,

        @NotBlank
        String hostname,

        @NotNull
        UUID templateId,

        @NotNull
        Integer cores,

        @NotNull
        Integer memory,

        @NotNull
        Integer disk,

        String password,

        Boolean nesting
) {
}
