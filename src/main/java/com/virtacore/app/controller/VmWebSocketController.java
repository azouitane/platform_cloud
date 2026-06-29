package com.virtacore.app.controller;

import com.virtacore.app.dto.response.LxcResponse;
import com.virtacore.app.dto.response.VirtualMachineResponse;
import com.virtacore.app.service.LxcContainerService;
import com.virtacore.app.service.impl.ProxmoxVmService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class VmWebSocketController {

    private final ProxmoxVmService vmService;
    private final LxcContainerService lxcContainerService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Liste des VM
     */
    @MessageMapping("/vms")
    public void getAllVms() {

        messagingTemplate.convertAndSend(
                "/topic/vms",
                vmService.getAllVms()
        );
    }

    /**
     * Liste des LXC
     */
    @MessageMapping("/lxc")
    public void getAllLxc() {

        messagingTemplate.convertAndSend(
                "/topic/lxc",
                lxcContainerService.findAll()
        );
    }

    /**
     * Détails d'une VM
     */
    @MessageMapping("/vms/{id}")
    public void getVm(
            @DestinationVariable UUID id
    ) {

        VirtualMachineResponse vm =
                vmService.getVmById(id);

        messagingTemplate.convertAndSend(
                "/topic/vms/" + id,
                vm
        );
    }

    /**
     * Détails d'un LXC
     */
    @MessageMapping("/lxc/{id}")
    public void getLxc(
            @DestinationVariable UUID id
    ) {

        LxcResponse lxc =
                lxcContainerService.findById(id);

        messagingTemplate.convertAndSend(
                "/topic/lxc/" + id,
                lxc
        );
    }

    /**
     * Monitoring d'une VM
     */
    @MessageMapping("/vms/{id}/monitor")
    public void monitorVm(
            @DestinationVariable UUID id
    ) {

        messagingTemplate.convertAndSend(
                "/topic/vms/" + id + "/status",
                vmService.getVmMonitor(id)
        );
    }

    /**
     * Monitoring d'un LXC
     */
    @MessageMapping("/lxc/{id}/monitor")
    public void monitorLxc(
            @DestinationVariable UUID id
    ) {

        messagingTemplate.convertAndSend(
                "/topic/lxc/" + id + "/status",
                lxcContainerService.getLxcMonitor(id)
        );
    }
}