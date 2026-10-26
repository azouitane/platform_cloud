package com.platformcloud.infrastructure.infrastructure.persistence.repository;

import com.platformcloud.infrastructure.infrastructure.persistence.entity.NodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NodeJpaReposirty extends JpaRepository<NodeEntity, UUID> {
}
