package com.virtacore.app.service.impl;


import com.virtacore.app.dto.request.vm.CreateVmRequest;
import com.virtacore.app.dto.response.ProxmoxResponse;
import com.virtacore.app.entity.vm.Cluster;
import com.virtacore.app.entity.vm.Node;
import com.virtacore.app.entity.vm.Template;
import com.virtacore.app.exception.ValidationException;
import com.virtacore.app.proxmox.ProxmoxClientFactory;
import com.virtacore.app.repository.ClusterRepository;
import com.virtacore.app.repository.TemplateRepository;
import com.virtacore.app.repository.VirtualMachineRepository;
import com.virtacore.app.service.NodeService;


import lombok.RequiredArgsConstructor;


import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;


import java.util.Map;
import java.util.UUID;



@Service
@RequiredArgsConstructor
public class ProxmoxVmService {


    private final ClusterRepository clusterRepository;
    private final VirtualMachineRepository virtualMachineRepository;
    private final TemplateRepository templateRepository;

    private final NodeService nodeService;

    private final ProxmoxClientFactory proxmoxClientFactory;



    public String createVm(
            CreateVmRequest request, UUID id
    ) throws InterruptedException {


        // ==========================
        // GET CLUSTER
        // ==========================


        Cluster cluster =
                clusterRepository.findById(
                                request.clusterId()
                        )
                        .orElseThrow(
                                () -> new ValidationException(
                                        "Cluster not found"
                                )
                        );

        Template template = templateRepository.findByProxmoxTemplateId(request.templateId())
                .orElseThrow(() -> new ValidationException("Template not found"));


        // ==========================
        // GET NODE
        // ==========================


        Node node = nodeService.findAvailableNodeEntity(cluster.getId());



        String nodeName =
                node.getName();



        WebClient proxmox = proxmoxClientFactory.create(cluster);



        // ==========================
        // NEXT VM ID
        // ==========================


        ProxmoxResponse idResponse =
                proxmox.get()

                        .uri("/cluster/nextid")

                        .retrieve()

                        .bodyToMono(ProxmoxResponse.class)

                        .block();



        String vmId =
                idResponse.data().toString();



        // ==========================
        // CLONE TEMPLATE
        // ==========================


        ProxmoxResponse cloneResponse =


                proxmox.post()


                        .uri(
                                "/nodes/{node}/qemu/{vmid}/clone",
                                nodeName,
                                request.templateId()
                        )


                        .contentType(
                                MediaType.APPLICATION_FORM_URLENCODED
                        )


                        .body(

                                BodyInserters
                                        .fromFormData(
                                                "newid",
                                                vmId
                                        )

                                        .with(
                                                "name",
                                                request.name()
                                        )

                                        .with(
                                                "full",
                                                "0"
                                        )
                        )


                        .retrieve()

                        .bodyToMono(ProxmoxResponse.class)

                        .block();



        String upid = cloneResponse.data().toString();


        waitTask(
                proxmox,
                nodeName,
                upid
        );


        // ==========================
        // CONFIG VM
        // ==========================


        proxmox.put()

                .uri(
                        "/nodes/{node}/qemu/{vmid}/config",
                        nodeName,
                        vmId
                )


                .contentType(
                        MediaType.APPLICATION_FORM_URLENCODED
                )


                .body(

                        BodyInserters
                                .fromFormData(
                                        "cores",
                                        request.cpu().toString()
                                )

                                .with(
                                        "memory",
                                        request.memory().toString()
                                )

                                .with(
                                        "ciuser",
                                        request.username()
                                )

                                .with(
                                        "cipassword",
                                        request.password()
                                )

                                .with(
                                        "ipconfig0",
                                        "ip=dhcp"
                                )

                                .with(
                                        "agent",
                                        "1"
                                )

                )


                .retrieve()

                .bodyToMono(String.class)

                .block();



        // ==========================
        // RESIZE DISK
        // ==========================


        if(request.disk()!=null){


            proxmox.put()

                    .uri(
                            "/nodes/{node}/qemu/{vmid}/resize",
                            nodeName,
                            vmId
                    )


                    .contentType(
                            MediaType.APPLICATION_FORM_URLENCODED
                    )


                    .body(

                            BodyInserters
                                    .fromFormData(
                                            "disk",
                                            "scsi0"
                                    )

                                    .with(
                                            "size",
                                            "+"+request.disk()+"G"
                                    )
                    )


                    .retrieve()

                    .bodyToMono(String.class)

                    .block();

        }


        // ==========================
        // START VM
        // ==========================


        proxmox.post()

                .uri(
                        "/nodes/{node}/qemu/{vmid}/status/start",
                        nodeName,
                        vmId
                )

                .retrieve()

                .bodyToMono(String.class)

                .block();


        return vmId;

    }







    private void waitTask(
            WebClient proxmox,
            String node,
            String upid
    ) throws InterruptedException {



        while(true){


            ProxmoxResponse response =


                    proxmox.get()

                            .uri(
                                    "/nodes/{node}/tasks/{upid}/status",
                                    node,
                                    upid
                            )


                            .retrieve()

                            .bodyToMono(ProxmoxResponse.class)

                            .block();



            Map data =
                    (Map) response.data();



            String status =
                    String.valueOf(
                            data.get("status")
                    );



            if(status.equals("stopped")){


                if(
                        "OK".equals(
                                data.get("exitstatus")
                        )
                ){
                    return;
                }



                throw new RuntimeException(
                        "TASK FAILED : "
                                + data
                );

            }



            Thread.sleep(3000);

        }

    }

}