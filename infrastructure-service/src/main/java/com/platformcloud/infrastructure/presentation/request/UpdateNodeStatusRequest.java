package com.platformcloud.infrastructure.presentation.request;

import com.platformcloud.infrastructure.domain.enums.NodeStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateNodeStatusRequest(
        @NotNull(message = "Node status is required")
        NodeStatus status
) {}