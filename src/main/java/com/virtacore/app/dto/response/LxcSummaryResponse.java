package com.virtacore.app.dto.response;

import com.virtacore.app.Enums.VmStatus;

import java.util.UUID;

public record LxcSummaryResponse(
        UUID id,
        Long lxcId,

        String hostname,

        String host,

        String ip,
        String osVersion,

        VmStatus status,

        Double cpuPercent,
        Double memoryPercent
) {
}
