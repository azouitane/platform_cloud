package com.virtacore.app.dto.response;

import java.util.UUID;

public record TemplateResponse(

        UUID id,

        Long proxmoxTemplateId,

        String name,

        String os,

        String version

        ) {
}
