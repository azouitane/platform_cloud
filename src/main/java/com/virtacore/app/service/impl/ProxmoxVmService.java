package com.virtacore.app.service.impl;

import com.virtacore.app.dto.request.CreateVmRequest;
import com.virtacore.app.dto.response.ProxmoxResponse;
import com.virtacore.app.repository.UserRepository;
import com.virtacore.app.repository.VirtualMachineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;


@Service
@RequiredArgsConstructor
public class ProxmoxVmService {


    private final WebClient proxmoxWebClient;
    private final UserRepository userRepository;
    private final VirtualMachineRepository virtualMachineRepository;

    private static final String NODE = "pve1";
    private static final int TEMPLATE_ID = 9000;


    public String createVm(CreateVmRequest request) throws InterruptedException {


        // 1 - get next id

        ProxmoxResponse response =
                proxmoxWebClient.get()
                        .uri("/cluster/nextid")
                        .retrieve()
                        .bodyToMono(ProxmoxResponse.class)
                        .block();


        String vmId = response.data().toString();



        // 2 - clone template

        String task = proxmoxWebClient.post()
                .uri("/nodes/{node}/qemu/{vmid}/clone",
                        NODE, request.templateId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(
                        BodyInserters
                                .fromFormData("newid", vmId)
                                .with("name", request.name())
                                .with("full", "1")
                )
                .retrieve()
                .bodyToMono(String.class)
                .block();



        // wait clone

        Thread.sleep(15000);



        // 3 - configure VM

        proxmoxWebClient.put()
                .uri("/nodes/{node}/qemu/{vmid}/config",
                        NODE,
                        vmId)

                .contentType(MediaType.APPLICATION_FORM_URLENCODED)

                .body(
                        BodyInserters
                                .fromFormData("cores",
                                        String.valueOf(request.cpu()))

                                .with("memory",
                                        String.valueOf(request.memory()))

                                .with("ciuser",
                                        request.username())

                                .with("cipassword",
                                        request.password())

                                .with("agent",
                                        "1")

                                .with("ipconfig0",
                                        "ip=dhcp")

                                .with("serial0",
                                        "socket")

                )

                .retrieve()
                .bodyToMono(String.class)
                .block();



        // 4 start


        proxmoxWebClient.post()
                .uri("/nodes/{node}/qemu/{vmid}/status/start",
                        NODE,
                        vmId)

                .retrieve()
                .bodyToMono(String.class)
                .block();



        return vmId;

    }

}