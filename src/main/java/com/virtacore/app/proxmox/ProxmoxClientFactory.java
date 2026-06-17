package com.virtacore.app.proxmox;

import com.virtacore.app.entity.vm.Cluster;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class ProxmoxClientFactory {


    private final WebClient.Builder builder;



    public WebClient create(
            Cluster cluster
    ){


        return builder
                .baseUrl(
                        "https://"
                                + cluster.getHost()
                                + ":"
                                + cluster.getApiPort()
                                + "/api2/json"
                )
                .defaultHeader(
                        "Authorization",
                        "PVEAPIToken="
                                + cluster.getTokenId()
                                + "="
                                + cluster.getTokenSecret()
                )
                .build();

    }
}