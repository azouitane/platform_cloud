package com.virtacore.app.controller;

import com.virtacore.app.dto.request.vm.CreateVmRequest;
import com.virtacore.app.security.CustomUserDetails;
import com.virtacore.app.service.impl.ProxmoxVmService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/vms")
@RequiredArgsConstructor
public class VmController {

    private final ProxmoxVmService vmService;

    @PostMapping
    public ResponseEntity<?> createVm(@Valid
            @RequestBody CreateVmRequest request, @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws InterruptedException {

        String vmId = vmService.createVm(request,userDetails.getId());

        return ResponseEntity.ok(
                Map.of(
                        "message", "VM created successfully",
                        "vm", vmId
                )
        );
    }
}