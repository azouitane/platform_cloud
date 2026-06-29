package com.virtacore.app.service.impl;


import com.virtacore.app.Enums.VmStatus;
import com.virtacore.app.dto.request.vm.CreateVmRequest;
import com.virtacore.app.dto.response.*;
import com.virtacore.app.entity.user.User;
import com.virtacore.app.entity.vm.Cluster;
import com.virtacore.app.entity.vm.Node;
import com.virtacore.app.entity.vm.Template;
import com.virtacore.app.entity.vm.VirtualMachine;
import com.virtacore.app.exception.ValidationException;
import com.virtacore.app.mapper.VirtualMachineMapper;
import com.virtacore.app.proxmox.ProxmoxClientFactory;
import com.virtacore.app.repository.ClusterRepository;
import com.virtacore.app.repository.TemplateRepository;
import com.virtacore.app.repository.UserRepository;
import com.virtacore.app.repository.VirtualMachineRepository;
import com.virtacore.app.service.NodeService;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;


import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;


import java.util.List;
import java.util.Map;
import java.util.UUID;



@Service
@RequiredArgsConstructor
@Transactional
public class ProxmoxVmService {


    private final ClusterRepository clusterRepository;
    private final VirtualMachineRepository virtualMachineRepository;
    private final VirtualMachineMapper virtualMachineMapper;
    private final UserRepository userRepository;
    private final TemplateRepository templateRepository;
    private final NodeService nodeService;
    private final ProxmoxClientFactory proxmoxClientFactory;




    public List<VmSummaryResponse> getAllVms() {

        List<VirtualMachine> virtualMachines =
                virtualMachineRepository.findAll();

        return virtualMachines.stream()
                .map(vm -> {

                    try {

                        WebClient proxmox =
                                proxmoxClientFactory.create(vm.getCluster());

                        ProxmoxResponse<Map<String,Object>> response =
                                proxmox.get()
                                        .uri(
                                                "/nodes/{node}/qemu/{vmid}/status/current",
                                                vm.getNode(),
                                                vm.getVmId()
                                        )
                                        .retrieve()
                                        .bodyToMono(ProxmoxResponse.class)
                                        .block();

                        Map<String, Object> data = response.data();

                        String proxmoxStatus =
                                (String) data.get("status");

                        vm.setStatus(
                                VmStatus.valueOf(
                                        proxmoxStatus.toUpperCase()
                                )
                        );

                        getVmIpAddress(vm.getId());

                        double cpuPercent = round(
                                (((Number) data.get("cpu")).doubleValue() * 100)
                                        / ((Number) data.get("cpus")).intValue()
                        );

                        double memoryPercent = round(
                                ((Number) data.get("mem")).doubleValue()
                                        / ((Number) data.get("maxmem")).doubleValue()
                                        * 100
                        );

                        return new VmSummaryResponse(
                                vm.getId(),
                                vm.getVmId(),
                                vm.getName(),
                                vm.getNode(),
                                vm.getIpAddress(),
                                vm.getTemplate().getName(),
                                vm.getStatus(),
                                cpuPercent,
                                memoryPercent
                        );

                    } catch (Exception e) {

                        return new VmSummaryResponse(
                                vm.getId(),
                                vm.getVmId(),
                                vm.getName(),
                                vm.getNode(),
                                vm.getIpAddress(),
                                vm.getTemplate().getName(),
                                vm.getStatus(),
                                0.0,
                                0.0
                        );
                    }
                })
                .toList();
    }
    public VirtualMachineResponse getVmById(UUID id) {

        VirtualMachine vm = virtualMachineRepository.findById(id)
                .orElseThrow(() ->
                        new ValidationException("VM not found")
                );

        return virtualMachineMapper.toResponse(vm);


    }



    public String createVm(CreateVmRequest request, UUID id) throws InterruptedException {


        // ==========================
        // VALIDATE NAME
        // ==========================
        String vmName = request.name();

        vmName = vmName.trim();

        if (!vmName.matches("^[a-zA-Z][a-zA-Z0-9-]{0,62}$")) {
            throw new ValidationException(
                    "VM name must be valid DNS name: start with letter, only a-z, 0-9, -, max 63 chars. Got: " + vmName
            );
        }

        // ==========================
        // GET CLUSTER
        // ==========================

        Cluster cluster = clusterRepository.findById(request.clusterId())
                        .orElseThrow(() -> new ValidationException("Cluster not found"));

        Template template = templateRepository.findByProxmoxTemplateId(request.templateId())
                .orElseThrow(() -> new ValidationException("Template not found"));

        User owner = userRepository.findById(id)
                .orElseThrow(() -> new ValidationException("User not found"));

        // ==========================
        // GET NODE
        // ==========================


        Node node = nodeService.findAvailableNodeEntity(cluster.getId());



        String nodeName = node.getName();

        WebClient proxmox = proxmoxClientFactory.create(cluster);


        // ==========================
        // NEXT VM ID
        // ==========================

        ProxmoxResponse<Object> idResponse = proxmox.get()
                .uri("/cluster/nextid")
                .retrieve()
                .bodyToMono(ProxmoxResponse.class)
                .block();
        System.out.println("NEXTID RESPONSE = " + idResponse);
        String vmId = idResponse.data().toString();

        // ==========================
        // CLONE TEMPLATE
        // ==========================
        try {
            ProxmoxResponse cloneResponse = proxmox.post()

                    .uri("/nodes/{node}/qemu/{vmid}/clone", nodeName, request.templateId())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData("newid", vmId)
                            .with("name",vmName)
                            .with("full", "1"))
                    .retrieve()
                    .bodyToMono(ProxmoxResponse.class)
                    .block();


            String upid = cloneResponse.data().toString();


            waitTask(proxmox, nodeName, upid);
        } catch(WebClientResponseException e){
            System.out.println("PROXMOX ERROR = " + e.getResponseBodyAsString());
            throw new RuntimeException("Failed to clone VM: " + e.getResponseBodyAsString(), e);
        }


        // ==========================
        // CONFIG VM
        // ==========================

        proxmox.put()
                .uri("/nodes/{node}/qemu/{vmid}/config", nodeName, vmId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("cores", request.cpu().toString())
                        .with("memory", request.memory().toString())
                        .with("ciuser", request.username())
                        .with("cipassword", request.password())
                        .with("ipconfig0", "ip=dhcp")
                        .with("agent", "1")
                        .with("ciupgrade", "0")
                )

                .retrieve()
                .bodyToMono(String.class)
                .block();


        // ==========================
        // RESIZE DISK
        // ==========================


        if(request.disk()!=null){

            proxmox.put()
                    .uri("/nodes/{node}/qemu/{vmid}/resize", nodeName, vmId)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData("disk", "scsi0")
                            .with("size", "+"+request.disk()+"G")
                    )
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

        }


        // ==========================
        // START VM
        // ==========================


        proxmox.post()

                .uri("/nodes/{node}/qemu/{vmid}/status/start", nodeName, vmId)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        VirtualMachine vm = new VirtualMachine();
        vm.setVmId(Long.valueOf(vmId));
        vm.setName(request.name());
        vm.setNode(nodeName);
        vm.setCpu(request.cpu());
        vm.setMemory(request.memory());
        vm.setDisk(request.disk());
        vm.setStatus(VmStatus.RUNNING);
        vm.setCluster(cluster);
        vm.setProxmoxNode(node);
        vm.setTemplate(template);
        vm.setOwner(owner);
        virtualMachineRepository.save(vm);

        return vmId;

    }



    public void startVm(UUID vmUuid) {

        VirtualMachine vm = getVm(vmUuid);

        Cluster cluster = vm.getCluster();

        WebClient proxmox = proxmoxClientFactory.create(cluster);

        proxmox.post()
                .uri(
                        "/nodes/{node}/qemu/{vmid}/status/start",
                        vm.getNode(),
                        vm.getVmId()
                )
                .retrieve()
                .bodyToMono(String.class)
                .block();

        vm.setStatus(VmStatus.RUNNING);

        virtualMachineRepository.save(vm);
    }


    public void shutdownVm(UUID vmUuid) {

        VirtualMachine vm = getVm(vmUuid);

        Cluster cluster = vm.getCluster();

        WebClient proxmox = proxmoxClientFactory.create(cluster);

        proxmox.post()
                .uri(
                        "/nodes/{node}/qemu/{vmid}/status/shutdown",
                        vm.getNode(),
                        vm.getVmId()
                )
                .retrieve()
                .bodyToMono(String.class)
                .block();

        vm.setStatus(VmStatus.STOPPED);

        virtualMachineRepository.save(vm);
    }


    public void restartVm(UUID vmUuid) {

        VirtualMachine vm = getVm(vmUuid);

        Cluster cluster = vm.getCluster();

        WebClient proxmox = proxmoxClientFactory.create(cluster);

        proxmox.post()
                .uri(
                        "/nodes/{node}/qemu/{vmid}/status/reboot",
                        vm.getNode(),
                        vm.getVmId()
                )
                .retrieve()
                .bodyToMono(String.class)
                .block();

        vm.setStatus(VmStatus.RUNNING);

        virtualMachineRepository.save(vm);
    }



    public void stopVm(UUID vmUuid) {

        VirtualMachine vm = getVm(vmUuid);

        Cluster cluster = vm.getCluster();

        WebClient proxmox = proxmoxClientFactory.create(cluster);

        proxmox.post()
                .uri(
                        "/nodes/{node}/qemu/{vmid}/status/stop",
                        vm.getNode(),
                        vm.getVmId()
                )
                .retrieve()
                .bodyToMono(String.class)
                .block();

        vm.setStatus(VmStatus.STOPPED);

        virtualMachineRepository.save(vm);
    }


    @Transactional
    public void deleteVm(UUID id) {

        VirtualMachine vm = getVm(id);

        Cluster cluster = vm.getCluster();

        WebClient proxmox = proxmoxClientFactory.create(cluster);

        String node =
                vm.getNode();

        Long vmId =
                vm.getVmId();

        // ==========================
        // STOP VM FIRST
        // ==========================

        try {

            proxmox.post()
                    .uri(
                            "/nodes/{node}/qemu/{vmid}/status/stop",
                            node,
                            vmId
                    )
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();


            Thread.sleep(1500);

        } catch (Exception ignored) {

            // VM already stopped
        }

        // ==========================
        // DELETE FROM PROXMOX
        // ==========================

        try {
            proxmox.delete()
                    .uri(
                            "/nodes/{node}/qemu/{vmid}",
                            node,
                            vmId
                    )
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // ==========================
            // DELETE DATABASE
            // ==========================

            virtualMachineRepository.delete(vm);

        }catch (Exception e){

            // ==========================
            // DELETE DATABASE
            // ==========================
            virtualMachineRepository.delete(vm);
        }

    }

    public String getVmIpAddress(UUID id) {

        VirtualMachine vm = getVm(id);

        WebClient proxmox =
                proxmoxClientFactory.create(vm.getCluster());


        ProxmoxResponse response =
                proxmox.get()
                        .uri(
                                "/nodes/{node}/qemu/{vmid}/agent/network-get-interfaces",
                                vm.getNode(),
                                vm.getVmId()
                        )
                        .retrieve()
                        .bodyToMono(ProxmoxResponse.class)
                        .block();


        Map<String,Object> data =
                (Map<String,Object>) response.data();


        List<Map<String,Object>> interfaces =
                (List<Map<String,Object>>) data.get("result");


        for(Map<String,Object> network : interfaces){


            String name =
                    String.valueOf(network.get("name"));


            // skip localhost
            if(name.equals("lo")){
                continue;
            }


            List<Map<String,Object>> ips =
                    (List<Map<String,Object>>)
                            network.get("ip-addresses");


            for(Map<String,Object> ip : ips){


                String type =
                        String.valueOf(
                                ip.get("ip-address-type")
                        );


                String address =
                        String.valueOf(
                                ip.get("ip-address")
                        );

                vm.setIpAddress(address);
                virtualMachineRepository.save(vm);
                if(
                        type.equals("ipv4")
                                &&
                                !address.equals("127.0.0.1")
                ){

                    return address;
                }

            }
        }


        return null;
    }




    public VmStatusResponse getVmMonitor(UUID id) {

        VirtualMachine vm = getVm(id);

        WebClient proxmox =
                proxmoxClientFactory.create(vm.getCluster());


        ProxmoxResponse<Map<String,Object>> response =
                proxmox.get()
                        .uri(
                                "/nodes/{node}/qemu/{vmid}/status/current",
                                vm.getNode(),
                                vm.getVmId()
                        )
                        .retrieve()
                        .bodyToMono(ProxmoxResponse.class)
                        .block();


        Map<String,Object> data = response.data();

        String proxmoxStatus = (String) data.get("status");

        try {

            vm.setStatus(
                    VmStatus.valueOf(
                            proxmoxStatus.toUpperCase()
                    )
            );

        } catch (IllegalArgumentException e) {

            vm.setStatus(VmStatus.ERROR);
        }

        virtualMachineRepository.save(vm);

        double cpu =
                doubleValue(data, "cpu");


        int cpus =
                intValue(data, "cpus");


        long mem =
                longValue(data, "mem");


        long maxmem =
                longValue(data, "maxmem");


        return new VmStatusResponse(

                round(
                        (cpu * 100) / cpus
                ),


                round(
                        mem / 1024.0 / 1024 / 1024
                ),


                round(
                        ((double) mem / maxmem) * 100
                ),


                round(
                        longValue(data,"diskread")
                                / 1024.0 / 1024
                ),


                round(
                        longValue(data,"diskwrite")
                                / 1024.0 / 1024
                ),


                round(
                        longValue(data,"netin")
                                / 1024.0 / 1024
                ),


                round(
                        longValue(data,"netout")
                                / 1024.0 / 1024
                ),


                longValue(data,"uptime") / 60

        );
    }


    public List<VirtualMachineSummaryResponse> getVmbyStatus(VmStatus status) {
        return virtualMachineRepository.findByStatus(status)
                .stream()
                .map(virtualMachineMapper::toSummaryResponse).toList();
    }

    private void waitTask(WebClient proxmox, String node, String upid) throws InterruptedException {
        while(true){


            ProxmoxResponse response =


                    proxmox.get()
                            .uri("/nodes/{node}/tasks/{upid}/status", node, upid)
                            .retrieve()
                            .bodyToMono(ProxmoxResponse.class)
                            .block();
            Map data = (Map) response.data();

            String status = String.valueOf(data.get("status"));

            if(status.equals("stopped")){

                if(
                        "OK".equals(data.get("exitstatus"))
                ){
                    return;
                }

                throw new RuntimeException(
                        "TASK FAILED : " + data
                );
            }

            Thread.sleep(3000);

        }

    }

    private VirtualMachine getVm(UUID vmId) {
        return virtualMachineRepository.findById(vmId)
                .orElseThrow(() -> new ValidationException("VM not found"));
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


    private double round(double value){

        return Math.round(value * 100.0) / 100.0;
    }
}