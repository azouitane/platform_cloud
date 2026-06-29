package com.virtacore.app.repository;

import com.virtacore.app.entity.lxc.LxcTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LxcTemplateRepository extends JpaRepository<LxcTemplate, UUID> {
    boolean existsByOstemplate(String ostemplate);
}
