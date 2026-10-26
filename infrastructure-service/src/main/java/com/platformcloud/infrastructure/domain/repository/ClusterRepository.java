package com.platformcloud.infrastructure.domain.repository;

import com.platformcloud.infrastructure.domain.model.Cluster;

import java.util.*;

public interface ClusterRepository {
    Cluster save(Cluster cluster);

    Optional<Cluster> findById(UUID id);

    List<Cluster> findAll();

    Optional<Cluster> findByName(String name);

    boolean existsByName(String name);

    boolean existsByHost(String host);

    void deleteById(UUID id);
}
