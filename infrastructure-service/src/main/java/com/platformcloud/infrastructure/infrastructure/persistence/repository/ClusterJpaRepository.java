package com.platformcloud.infrastructure.infrastructure.persistence.repository;

import com.platformcloud.infrastructure.infrastructure.persistence.entity.ClusterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClusterJpaRepository
        extends JpaRepository<ClusterEntity, UUID> {

    Optional<ClusterEntity> findByName(String name);

    boolean existsByName(String name);

    boolean existsByHost(String host);
}