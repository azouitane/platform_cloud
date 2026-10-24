package com.platformcloud.infrastructure.infrastructure.persistence.repository;

import com.platformcloud.infrastructure.infrastructure.persistence.entity.NodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NodeReposirty extends JpaRepository<NodeEntity, UUID> {
}
