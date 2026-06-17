package com.virtacore.app.dto.response;

import java.util.UUID;

public record ClusterResponse(
        UUID id,

        String name,

        String host,

        Integer apiPort,

        boolean active
) {
}
