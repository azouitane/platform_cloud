package com.platformcloud.infrastructure.application.service;

import com.platformcloud.infrastructure.domain.enums.NodeStatus;
import com.platformcloud.infrastructure.domain.model.Node;

import java.util.List;
import java.util.UUID;

public interface NodeService {

    Node create(Node node);

    Node findById(UUID id);

    List<Node> findAll();

    List<Node> findByClusterId(UUID clusterId);

    Node updateStatus(UUID clusterId, UUID nodeId, NodeStatus status);

    void delete(UUID id);
}