package com.virtacore.app.dto.response;

import java.util.UUID;

public record VmIpResponse(
        UUID vmId,
        String ipAddress
) {
}
