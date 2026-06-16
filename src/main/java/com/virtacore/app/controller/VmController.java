package com.virtacore.app.controller;

import com.virtacore.app.dto.request.CreateVmRequest;
import com.virtacore.app.service.impl.ProxmoxVmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/vms")
@RequiredArgsConstructor
public class VmController {

    private final ProxmoxVmService vmService;

    @PostMapping
    public ResponseEntity<?> createVm(
            @RequestBody CreateVmRequest request
    ) throws InterruptedException {

        String vmId = vmService.createVm(request);

        return ResponseEntity.ok(
                Map.of(
                        "message", "VM created successfully",
                        "vmId", vmId
                )
        );
    }
}