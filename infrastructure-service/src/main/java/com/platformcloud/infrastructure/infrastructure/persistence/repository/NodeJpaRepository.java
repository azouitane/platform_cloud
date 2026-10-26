package com.platformcloud.infrastructure.infrastructure.persistence.repository;

import com.platformcloud.infrastructure.infrastructure.persistence.entity.NodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NodeJpaRepository extends JpaRepository<NodeEntity, UUID> {

    List<NodeEntity> findByCluster_Id(UUID clusterId);

    Optional<NodeEntity> findByCluster_IdAndName(
            UUID clusterId,
            String name
    );

    boolean existsByCluster_IdAndName(
            UUID clusterId,
            String name
    );
}