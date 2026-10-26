package com.platformcloud.infrastructure.domain.repository;

import com.platformcloud.infrastructure.domain.model.Node;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NodeRepository {

    Node save(Node node);

    Optional<Node> findById(UUID id);

    List<Node> findAll();

    List<Node> findByClusterId(UUID clusterId);

    Optional<Node> findByClusterIdAndName(
            UUID clusterId,
            String name
    );

    boolean existsByClusterIdAndName(
            UUID clusterId,
            String name
    );

    void deleteById(UUID id);
}