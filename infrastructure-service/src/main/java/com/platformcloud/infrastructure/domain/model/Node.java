package com.platformcloud.infrastructure.domain.model;


import com.platformcloud.infrastructure.domain.enums.NodeStatus;

import java.time.Instant;
import java.util.UUID;

public record Node(
        UUID id,
        UUID clusterId,
        String name,
        NodeStatus status,
        Instant createdAt
) {
    public Node {
        if (clusterId == null) {
            throw new IllegalArgumentException("Cluster ID is required");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Node name is required");
        }
    }
}
