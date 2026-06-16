package com.virtacore.app.repository;

import com.virtacore.app.entity.vm.Template;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TemplateRepository extends JpaRepository<Template, UUID> {

    Optional<Template> findByProxmoxTemplateId(Long proxmoxTemplateId);

    Optional<Template> findByName(String name);
}
