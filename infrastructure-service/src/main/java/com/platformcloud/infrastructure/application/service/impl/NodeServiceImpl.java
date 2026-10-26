package com.platformcloud.infrastructure.application.service.impl;

import com.platformcloud.infrastructure.application.service.NodeService;
import com.platformcloud.infrastructure.domain.enums.NodeStatus;
import com.platformcloud.infrastructure.domain.exception.ResourceNotFoundException;
import com.platformcloud.infrastructure.domain.exception.ValidationException;
import com.platformcloud.infrastructure.domain.model.Node;
import com.platformcloud.infrastructure.domain.repository.ClusterRepository;
import com.platformcloud.infrastructure.domain.repository.NodeRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NodeServiceImpl implements NodeService {

    private final NodeRepository nodeRepository;
    private final ClusterRepository clusterRepository;

    @Override
    @Transactional
    public Node create(Node node) {

        if (clusterRepository.findById(node.clusterId()).isEmpty()) {
            throw new ResourceNotFoundException(
                    "Cluster not found: " + node.clusterId()
            );
        }

        if (nodeRepository.existsByClusterIdAndName(
                node.clusterId(),
                node.name()
        )) {
            throw new ValidationException(
                    "Node already exists in this cluster: " + node.name()
            );
        }

        return nodeRepository.save(node);
    }

    @Override
    public Node findById(UUID id) {
        return nodeRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Node not found: " + id
                        )
                );
    }

    @Override
    public List<Node> findAll() {
        return nodeRepository.findAll();
    }

    @Override
    public List<Node> findByClusterId(UUID clusterId) {
        return nodeRepository.findByClusterId(clusterId);
    }

    @Override
    @Transactional
    public Node updateStatus(
            UUID clusterId,
            UUID nodeId,
            NodeStatus status
    ) {
        if (status == null) {
            throw new ValidationException("Node status is required");
        }

        Node existing = nodeRepository.findById(nodeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Node not found: " + nodeId
                        )
                );

        if (!existing.clusterId().equals(clusterId)) {
            throw new ValidationException(
                    "Node does not belong to cluster: " + clusterId
            );
        }

        Node updated = new Node(
                existing.id(),
                existing.clusterId(),
                existing.name(),
                status,
                existing.createdAt()
        );

        return nodeRepository.save(updated);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        findById(id);
        nodeRepository.deleteById(id);
    }
}