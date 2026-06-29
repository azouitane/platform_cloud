package com.virtacore.app.dto.response;

public record VmStatusResponse(

        Double cpuPercent,

        Double memoryGB,
        Double memoryPercent,

        Double diskReadMB,
        Double diskWriteMB,

        Double networkInMB,
        Double networkOutMB,

        Long uptimeMinutes

) {}