package com.platformcloud.infrastructure.application.service;

import com.platformcloud.infrastructure.domain.model.Cluster;

import java.util.List;
import java.util.UUID;

public interface ClusterService {
    Cluster create(Cluster cluster);

    Cluster findById(UUID id);

    List<Cluster> findAll();

    Cluster update(Cluster cluster);

    void delete(UUID id);

    boolean verifyConnection(UUID id);
}
