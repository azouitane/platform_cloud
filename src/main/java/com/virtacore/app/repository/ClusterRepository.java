package com.virtacore.app.repository;

import com.virtacore.app.entity.vm.Cluster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClusterRepository extends JpaRepository<Cluster, UUID> {
    Optional<Cluster> findByName(String name);

    boolean existsByName(String name);

    boolean existsByHost(String host);
    Optional<Cluster> findByHost(String host);


    List<Cluster> findByActiveTrue();


    Optional<Cluster> findFirstByActiveTrue();
}
