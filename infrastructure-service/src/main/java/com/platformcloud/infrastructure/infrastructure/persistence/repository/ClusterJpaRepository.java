package com.platformcloud.infrastructure.infrastructure.persistence.repository;

import com.platformcloud.infrastructure.infrastructure.persistence.entity.ClusterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClusterJpaRepository extends JpaRepository<ClusterEntity, UUID> {
}
