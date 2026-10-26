package com.platformcloud.infrastructure.infrastructure.persistence.impl;

import com.platformcloud.infrastructure.domain.exception.ResourceNotFoundException;
import com.platformcloud.infrastructure.domain.exception.ValidationException;
import com.platformcloud.infrastructure.domain.model.Node;
import com.platformcloud.infrastructure.domain.repository.NodeRepository;
import com.platformcloud.infrastructure.infrastructure.persistence.entity.ClusterEntity;
import com.platformcloud.infrastructure.infrastructure.persistence.entity.NodeEntity;
import com.platformcloud.infrastructure.infrastructure.persistence.mapper.NodeMapper;
import com.platformcloud.infrastructure.infrastructure.persistence.repository.NodeJpaRepository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NodeRepositoryImpl implements NodeRepository {

    private final NodeJpaRepository jpaRepository;
    private final NodeMapper mapper;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public Node save(Node node) {

        NodeEntity entity;

        if (node.id() == null) {

            ClusterEntity clusterRef = entityManager.getReference(
                    ClusterEntity.class,
                    node.clusterId()
            );

            entity = new NodeEntity(
                    clusterRef,
                    node.name()
            );

        } else {

            entity = jpaRepository.findById(node.id())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Node not found: " + node.id()
                            )
                    );

            if (!entity.getCluster().getId().equals(node.clusterId())
                    || !entity.getName().equals(node.name())) {
                throw new ValidationException(
                        "Node cluster and name cannot be changed"
                );
            }

            entity.updateStatus(node.status());
        }

        return mapper.toDomain(jpaRepository.saveAndFlush(entity));
    }

    @Override
    public Optional<Node> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Node> findAll() {
        return mapper.toDomainList(jpaRepository.findAll());
    }

    @Override
    public List<Node> findByClusterId(UUID clusterId) {
        return mapper.toDomainList(
                jpaRepository.findByCluster_Id(clusterId)
        );
    }

    @Override
    public Optional<Node> findByClusterIdAndName(
            UUID clusterId,
            String name
    ) {
        return jpaRepository
                .findByCluster_IdAndName(clusterId, name)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByClusterIdAndName(
            UUID clusterId,
            String name
    ) {
        return jpaRepository.existsByCluster_IdAndName(
                clusterId,
                name
        );
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}