package com.virtacore.app.dto.response;

import java.util.UUID;

public record ClusterSummary(
        UUID id,

        String name
) {
}
