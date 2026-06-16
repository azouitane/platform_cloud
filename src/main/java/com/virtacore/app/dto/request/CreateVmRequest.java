package com.virtacore.app.dto.request;

public record CreateVmRequest (
        String name,
        Integer templateId,
        Integer cpu,
        Integer memory,
        Integer disk,
        String username,
        String password
){}
