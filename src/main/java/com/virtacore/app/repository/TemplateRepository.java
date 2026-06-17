package com.virtacore.app.repository;

import com.virtacore.app.entity.vm.Template;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TemplateRepository extends JpaRepository<Template, UUID> {

    List<Template> findByOsIgnoreCase(String os);

    boolean existsByProxmoxTemplateId(Long proxmoxTemplateId);

    Optional<Template> findByProxmoxTemplateId(Long proxmoxTemplateId);

    Optional<Template> findByName(String name);
}
