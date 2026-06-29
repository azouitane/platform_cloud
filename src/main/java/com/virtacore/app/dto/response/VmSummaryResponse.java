package com.virtacore.app.dto.response;

import com.virtacore.app.Enums.VmStatus;

import java.util.UUID;

public record VmSummaryResponse(
        UUID id,
        Long vmid,

        String name,
        String host,

        String ip,
        String osVersion,

        VmStatus status,

        Double cpuPercent,
        Double memoryPercent
) {
}
