package com.virtacore.app.dto.response;

import com.virtacore.app.Enums.VmStatus;

import java.util.UUID;

public record VirtualMachineSummaryResponse(


        UUID id,

        Long vmId,

        String name,

        String node,

        String nameOS
) {
}
