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
) { }
