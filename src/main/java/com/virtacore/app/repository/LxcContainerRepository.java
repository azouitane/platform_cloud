package com.virtacore.app.repository;

import com.virtacore.app.entity.lxc.LxcContainer;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LxcContainerRepository extends JpaRepository<LxcContainer, UUID> {
    List<LxcContainer> findByOwnerId(UUID ownerId);
    Optional<LxcContainer> findByLxcId(Long lxcId);
}
