package com.platformcloud.infrastructure.application.port;

import com.platformcloud.infrastructure.domain.model.Cluster;

public interface ProxmoxGateway {
    boolean verifyConnection(Cluster cluster);
}
