package com.platformcloud.infrastructure.presentation.response;

import com.platformcloud.infrastructure.domain.enums.NodeStatus;

import java.time.Instant;
import java.util.UUID;

public record NodeResponse(
        UUID id,
        UUID clusterId,
        String name,
        NodeStatus status,
        Instant createdAt
) {}