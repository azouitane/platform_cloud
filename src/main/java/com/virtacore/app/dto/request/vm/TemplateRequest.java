package com.virtacore.app.dto.request.vm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record TemplateRequest(

        @NotNull(message = "Proxmox template ID is required")
        Long proxmoxTemplateId,

        @NotBlank(message = "Template name is required")
        String name,

        @NotBlank(message = "Operating system is required")
        String os,

        @NotBlank(message = "Version is required")
        String version

) {
}