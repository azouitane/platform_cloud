package com.virtacore.app.service.impl;

import com.virtacore.app.Enums.VmStatus;
import com.virtacore.app.dto.request.lxc.CreateLxcRequest;
import com.virtacore.app.dto.response.*;
import com.virtacore.app.entity.lxc.LxcContainer;
import com.virtacore.app.entity.lxc.LxcTemplate;
import com.virtacore.app.entity.user.User;
import com.virtacore.app.entity.vm.Cluster;
import com.virtacore.app.entity.vm.Node;
import com.virtacore.app.entity.vm.VirtualMachine;
import com.virtacore.app.exception.ResourceNotFoundException;
import com.virtacore.app.exception.ValidationException;
import com.virtacore.app.mapper.LxcContainerMapper;
import com.virtacore.app.proxmox.ProxmoxClientFactory;
import com.virtacore.app.repository.*;
import com.virtacore.app.service.LxcContainerService;
import com.virtacore.app.service.NodeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.Math.round;


@Service
@RequiredArgsConstructor
@Transactional
public class LxcContainerServiceImpl implements LxcContainerService {


    private final LxcContainerRepository lxcRepository;
    private final LxcTemplateRepository templateRepository;
    private final UserRepository userRepository;
    private final ClusterRepository clusterRepository;
    private final ProxmoxVmService vmService;

    private final ProxmoxClientFactory proxmoxClientFactory;
    private final NodeService nodeService;
    private final LxcContainerMapper mapper;



    @Override
    public LxcResponse create(
            CreateLxcRequest request,
            UUID userId
    ) {


        String hostname = request.hostname().trim();


        if(!hostname.matches(
                "^[a-zA-Z][a-zA-Z0-9-]{0,62}$"
        )) {
            throw new ValidationException(
                    "Invalid hostname"
            );
        }



        Cluster cluster =
                clusterRepository.findById(
                                request.clusterId()
                        )
                        .orElseThrow(
                                () -> new ValidationException("Cluster not found")
                        );



        LxcTemplate template =
                templateRepository.findById(
                                request.templateId()
                        )
                        .orElseThrow(
                                () -> new ValidationException("Template not found")
                        );



        User owner =
                userRepository.findById(userId)
                        .orElseThrow(
                                () -> new ValidationException("User not found")
                        );



        Node node =
                nodeService.findAvailableNodeEntity(
                        request.clusterId()
                );



        WebClient proxmox =
                proxmoxClientFactory.create(cluster);



        Long lxcId =
                Long.valueOf(
                        proxmox.get()
                                .uri("/cluster/nextid")
                                .retrieve()
                                .bodyToMono(ProxmoxResponse.class)
                                .block()
                                .data()
                                .toString()
                );



        try {


            proxmox.post()
                    .uri(
                            "/nodes/{node}/lxc",
                            node.getName()
                    )
                    .body(
                            BodyInserters
                                    .fromFormData(
                                            "vmid",
                                            lxcId.toString()
                                    )
                                    .with(
                                            "ostemplate",
                                            template.getOstemplate()
                                    )
                                    .with(
                                            "hostname",
                                            hostname
                                    )
                                    .with(
                                            "password",
                                            request.password()
                                    )
                                    .with(
                                            "cores",
                                            request.cores().toString()
                                    )
                                    .with(
                                            "memory",
                                            request.memory().toString()
                                    )
                                    .with(
                                            "swap",
                                            request.swap().toString()
                                    )
                                    .with(
                                            "rootfs",
                                            "local-lvm:"+
                                                    request.disk()
                                    )
                                    .with(
                                            "net0",
                                            "name=eth0,bridge=vmbr0,ip=dhcp"
                                    )
                                    .with(
                                            "features",
                                            "nesting=1"
                                    )
                                    .with(
                                            "unprivileged",
                                            "1"
                                    )
                                    .with(
                                            "start",
                                            "1"
                                    )
                    )
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();


            // wait proxmox task
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ValidationException(
                        "LXC creation interrupted"
                );
            }

            LxcContainer container =
                    new LxcContainer();


            container.setLxcId(lxcId);

            container.setHostname(hostname);

            container.setCores(request.cores());

            container.setMemory(request.memory());

            container.setSwap(request.swap());

            container.setDisk(request.disk());


            container.setCluster(cluster);

            container.setTemplate(template);

            container.setOwner(owner);

            container.setProxmoxNode(node);


            container.setStatus(
                    VmStatus.RUNNING
            );


            container.setCreatedAt(
                    LocalDateTime.now()
            );


            return mapper.toResponse(
                    lxcRepository.save(container)
            );



        }catch(WebClientResponseException e){

            throw new ValidationException(
                    "Proxmox error : "
                            + e.getResponseBodyAsString()
            );
        }

    }

    @Override
    public List<LxcSummaryResponse> findAll() {

        return lxcRepository.findAll()
                .stream()
                .map(lxc -> {

                    try {

                        WebClient proxmox =
                                proxmoxClientFactory.create(
                                        lxc.getCluster()
                                );

                        ProxmoxResponse<Map<String, Object>> response =
                                proxmox.get()
                                        .uri(
                                                "/nodes/{node}/lxc/{vmid}/status/current",
                                                lxc.getProxmoxNode().getName(),
                                                lxc.getLxcId()
                                        )
                                        .retrieve()
                                        .bodyToMono(ProxmoxResponse.class)
                                        .block();

                        Map<String, Object> data =
                                response.data();

                        String proxmoxStatus =
                                String.valueOf(
                                        data.get("status")
                                );

                        lxc.setStatus(
                                VmStatus.valueOf(
                                        proxmoxStatus.toUpperCase()
                                )
                        );

                        String ip =
                                getLxcIpAddress(
                                        lxc.getId()
                                );

                        double cpuPercent =
                                round(
                                        ((Number) data.get("cpu"))
                                                .doubleValue()
                                                * 100
                                );

                        double memoryPercent =
                                round(
                                        (
                                                ((Number) data.get("mem"))
                                                        .doubleValue()
                                                        /
                                                        ((Number) data.get("maxmem"))
                                                                .doubleValue()
                                        ) * 100
                                );

                        return new LxcSummaryResponse(
                                lxc.getId(),
                                lxc.getLxcId(),
                                lxc.getHostname(),
                                lxc.getProxmoxNode().getName(),
                                lxc.getIpAddress(),
                                lxc.getTemplate().getName(),
                                lxc.getStatus(),
                                cpuPercent,
                                memoryPercent
                        );

                    } catch (Exception e) {

                        return new LxcSummaryResponse(
                                lxc.getId(),
                                lxc.getLxcId(),
                                lxc.getHostname(),
                                lxc.getProxmoxNode().getName(),
                                lxc.getIpAddress(),
                                lxc.getTemplate().getName(),
                                VmStatus.ERROR,
                                0.0,
                                0.0
                        );
                    }
                })
                .toList();
    }


    @Override
    public LxcResponse findById(UUID id){


        return mapper.toResponse(
                lxcRepository.findById(id)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("LXC")
                        )
        );
    }





    @Override
    public LxcResponse findByVmId(Long vmId){


        return mapper.toResponse(
                lxcRepository.findByLxcId(vmId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("LXC")
                        )
        );
    }




    @Override
    public void start(UUID id){

        action(
                id,
                "start",
                VmStatus.RUNNING
        );
    }




    @Override
    public void stop(UUID id){

        action(
                id,
                "stop",
                VmStatus.STOPPED
        );
    }





    @Override
    public void shutdown(UUID id){

        action(
                id,
                "shutdown",
                VmStatus.STOPPED
        );
    }




    @Override
    public void restart(UUID id){

        action(
                id,
                "reboot",
                VmStatus.RUNNING
        );
    }






    private void action(
            UUID id,
            String command,
            VmStatus status
    ){


        LxcContainer container =
                lxcRepository.findById(id)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("LXC")
                        );



        WebClient proxmox =
                proxmoxClientFactory.create(
                        container.getCluster()
                );



        proxmox.post()
                .uri(
                        "/nodes/{node}/lxc/{vmid}/status/{cmd}",
                        container.getProxmoxNode().getName(),
                        container.getLxcId(),
                        command
                )
                .retrieve()
                .bodyToMono(Object.class)
                .block();




        container.setStatus(status);

        lxcRepository.save(container);

    }






    @Override
    public void delete(UUID id) {

        LxcContainer container =
                lxcRepository.findById(id)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("LXC")
                        );


        WebClient proxmox =
                proxmoxClientFactory.create(
                        container.getCluster()
                );


        try {


            // check status
            ProxmoxResponse<Object> statusResponse =
                    proxmox.get()
                            .uri(
                                    "/nodes/{node}/lxc/{vmid}/status/current",
                                    container.getProxmoxNode().getName(),
                                    container.getLxcId()
                            )
                            .retrieve()
                            .bodyToMono(ProxmoxResponse.class)
                            .block();



            String status =
                    ((java.util.Map<?,?>)
                            statusResponse.data())
                            .get("status")
                            .toString();



            // stop only if running
            if(status.equals("running")) {


                proxmox.post()
                        .uri(
                                "/nodes/{node}/lxc/{vmid}/status/stop",
                                container.getProxmoxNode().getName(),
                                container.getLxcId()
                        )
                        .retrieve()
                        .bodyToMono(Object.class)
                        .block();


                Thread.sleep(3000);
            }



            // destroy
            proxmox.delete()
                    .uri(
                            "/nodes/{node}/lxc/{vmid}",
                            container.getProxmoxNode().getName(),
                            container.getLxcId()
                    )
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();



            lxcRepository.delete(container);



        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
            throw new ValidationException("Delete interrupted");


        } catch (WebClientResponseException e) {

            throw new ValidationException(
                    "Delete failed : "
                            + e.getResponseBodyAsString()
            );
        }
    }


    @Override
    public VmStatusResponse getLxcMonitor(UUID id) {

        LxcContainer lxc = lxcRepository.findById(id)
                .orElseThrow(
                        () -> new ValidationException("Container not found")
                );

        WebClient proxmox =
                proxmoxClientFactory.create(
                        lxc.getCluster()
                );

        ProxmoxResponse<Map<String, Object>> response =
                proxmox.get()
                        .uri(
                                "/nodes/{node}/lxc/{vmid}/status/current",
                                lxc.getProxmoxNode().getName(),
                                lxc.getLxcId()
                        )
                        .retrieve()
                        .bodyToMono(ProxmoxResponse.class)
                        .block();

        Map<String, Object> data = response.data();

        String proxmoxStatus =
                String.valueOf(data.get("status"));

        try {

            lxc.setStatus(
                    VmStatus.valueOf(
                            proxmoxStatus.toUpperCase()
                    )
            );

        } catch (Exception e) {

            lxc.setStatus(VmStatus.ERROR);
        }

        lxcRepository.save(lxc);

        double cpu =
                doubleValue(data, "cpu");

        int cpus =
                intValue(data, "cpus");

        long mem =
                longValue(data, "mem");

        long maxmem =
                longValue(data, "maxmem");

        double cpuUsage =
                cpus > 0
                        ? round((cpu * 100) / cpus)
                        : 0;

        return new VmStatusResponse(

                cpuUsage,

                round(
                        mem / 1024.0 / 1024 / 1024
                ),

                maxmem > 0
                        ? round(
                        ((double) mem / maxmem) * 100
                )
                        : 0,

                round(
                        longValue(data, "diskread")
                                / 1024.0 / 1024
                ),

                round(
                        longValue(data, "diskwrite")
                                / 1024.0 / 1024
                ),

                round(
                        longValue(data, "netin")
                                / 1024.0 / 1024
                ),

                round(
                        longValue(data, "netout")
                                / 1024.0 / 1024
                ),

                longValue(data, "uptime") / 60

        );
    }

    private String getLxcIpAddress(UUID id) {
        LxcContainer lxc = lxcRepository.findById(id)
                .orElseThrow(
                        () -> new ValidationException("Container not found")
                );

        WebClient proxmox =
                proxmoxClientFactory.create(
                        lxc.getCluster()
                );

        try {

            ProxmoxResponse<List<Map<String, Object>>> response =
                    proxmox.get()
                            .uri(
                                    "/nodes/{node}/lxc/{vmid}/interfaces",
                                    lxc.getProxmoxNode().getName(),
                                    lxc.getLxcId()
                            )
                            .retrieve()
                            .bodyToMono(ProxmoxResponse.class)
                            .block();

            List<Map<String, Object>> interfaces =
                    (List<Map<String, Object>>) response.data();

            for (Map<String, Object> network : interfaces) {

                String name =
                        String.valueOf(network.get("name"));

                if ("lo".equals(name)) {
                    continue;
                }

                List<Map<String, Object>> ips =
                        (List<Map<String, Object>>)
                                network.get("ip-addresses");

                if (ips == null) {
                    continue;
                }

                for (Map<String, Object> ip : ips) {

                    String type =
                            String.valueOf(
                                    ip.get("ip-address-type")
                            );

                    String address =
                            String.valueOf(
                                    ip.get("ip-address")
                            );

                    if ("inet".equals(type)
                            && !"127.0.0.1".equals(address)) {

                        lxc.setIpAddress(address);
                        lxcRepository.save(lxc);

                        return address;
                    }
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "Unable to get LXC IP: "
                            + e.getMessage()
            );
        }

        return null;
    }


    private double round(double value){

        return Math.round(value * 100.0) / 100.0;
    }
    private String stringValue(
            Map<String,Object> data,
            String key
    ){
        return (String) data.get(key);
    }


    private int intValue(
            Map<String,Object> data,
            String key
    ){
        return ((Number)data.get(key)).intValue();
    }


    private long longValue(Map<String, Object> map, String key) {

        Object value = map.get(key);

        if (value == null) {
            return 0L;
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        return 0L;
    }


    private double doubleValue(
            Map<String,Object> data,
            String key
    ){
        return ((Number)data.get(key)).doubleValue();
    }

}