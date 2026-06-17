package com.virtacore.app.dto.response;

import java.util.UUID;

public record NodeResponse(
        UUID id,

        String name,

        String host,

        UUID clusterId
) {
}
