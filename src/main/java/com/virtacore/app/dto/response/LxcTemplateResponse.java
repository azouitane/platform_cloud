package com.virtacore.app.dto.response;

import java.util.UUID;

public record LxcTemplateResponse(
        UUID id,
        String name
) {
}
