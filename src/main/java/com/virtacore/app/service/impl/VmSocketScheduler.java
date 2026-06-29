package com.virtacore.app.service.impl;

import com.virtacore.app.dto.response.LxcResponse;
import com.virtacore.app.dto.response.LxcSummaryResponse;
import com.virtacore.app.dto.response.VirtualMachineResponse;
import com.virtacore.app.dto.response.VmStatusResponse;
import com.virtacore.app.dto.response.VmSummaryResponse;
import com.virtacore.app.service.LxcContainerService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VmSocketScheduler {

    private final ProxmoxVmService vmService;
    private final LxcContainerService lxcContainerService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Envoie la liste des VM et LXC toutes les 5 secondes.
     */
    @Scheduled(fixedDelay = 2000)
    public void sendInventory() {

        List<VmSummaryResponse> vms = vmService.getAllVms();
        List<LxcSummaryResponse> lxcs = lxcContainerService.findAll();

        messagingTemplate.convertAndSend(
                "/topic/vms",
                vms
        );

        messagingTemplate.convertAndSend(
                "/topic/lxc",
                lxcs
        );
    }

    /**
     * Envoie les détails des VM toutes les 5 secondes.
     */
    @Scheduled(fixedDelay = 2000)
    public void sendVmDetails() {

        List<VmSummaryResponse> vms = vmService.getAllVms();

        for (VmSummaryResponse vm : vms) {

            VirtualMachineResponse response =
                    vmService.getVmById(vm.id());

            messagingTemplate.convertAndSend(
                    "/topic/vms/" + vm.id(),
                    response
            );
        }
    }

    /**
     * Envoie les détails des LXC toutes les 5 secondes.
     */
    @Scheduled(fixedDelay = 1000)
    public void sendLxcDetails() {

        List<LxcSummaryResponse> lxcs =
                lxcContainerService.findAll();

        for (LxcSummaryResponse lxc : lxcs) {

            LxcResponse response =
                    lxcContainerService.findById(lxc.id());

            messagingTemplate.convertAndSend(
                    "/topic/lxc/" + lxc.id(),
                    response
            );
        }
    }

    /**
     * Envoie les statistiques temps réel des VM chaque seconde.
     */
    @Scheduled(fixedDelay = 1000)
    public void sendVmStatus() {

        List<VmSummaryResponse> vms = vmService.getAllVms();

        for (VmSummaryResponse vm : vms) {

            VmStatusResponse status =
                    vmService.getVmMonitor(vm.id());

            messagingTemplate.convertAndSend(
                    "/topic/vms/" + vm.id() + "/status",
                    status
            );
        }
    }

    /**
     * Envoie les statistiques temps réel des LXC chaque seconde.
     */
    @Scheduled(fixedDelay = 1000)
    public void sendLxcStatus() {

        List<LxcSummaryResponse> lxcs =
                lxcContainerService.findAll();

        for (LxcSummaryResponse lxc : lxcs) {

            VmStatusResponse status =
                    lxcContainerService.getLxcMonitor(lxc.id());

            messagingTemplate.convertAndSend(
                    "/topic/lxc/" + lxc.id() + "/status",
                    status
            );
        }
    }
}