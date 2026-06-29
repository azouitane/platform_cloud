package com.virtacore.app.dto.response;

import com.virtacore.app.Enums.VmStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record VirtualMachineResponse(
        UUID id,

        Long vmId,

        String name,

        String node,

        Integer cpu,

        Integer memory,

        Integer disk,

        VmStatus status,

        String clusterName,

        String os,

        String version,

        LocalDateTime createdAt
) {
}
