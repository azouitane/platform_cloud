package com.virtacore.app.controller;

import com.virtacore.app.dto.request.vm.CreateVmRequest;
import com.virtacore.app.dto.response.VirtualMachineResponse;
import com.virtacore.app.dto.response.VmIpResponse;
import com.virtacore.app.security.CustomUserDetails;
import com.virtacore.app.service.impl.ProxmoxVmService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vms")
@RequiredArgsConstructor
public class VmController {

    private final ProxmoxVmService vmService;


    @GetMapping
    public ResponseEntity<List<VirtualMachineResponse>> getAllVms() {

        return ResponseEntity.ok(
                vmService.getAllVms()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<VirtualMachineResponse> getVmById(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                vmService.getVmById(id)
        );
    }
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

    @PostMapping("/{id}/start")
    public ResponseEntity<Void> start(@PathVariable UUID id) {
        vmService.startVm(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/shutdown")
    public ResponseEntity<Void> shutdown(@PathVariable UUID id) {
        vmService.shutdownVm(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/restart")
    public ResponseEntity<Void> restart(@PathVariable UUID id) {
        vmService.restartVm(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<Void> stop(@PathVariable UUID id) {
        vmService.stopVm(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVm(
            @PathVariable UUID id
    ) {

        vmService.deleteVm(id);

        return ResponseEntity.noContent()
                .build();
    }

    @GetMapping("/{id}/ip")
    public VmIpResponse getIpAddress(
            @PathVariable UUID id
    ) {

        String ip = vmService.getVmIpAddress(id);

        return new VmIpResponse(id, ip);
    }
}