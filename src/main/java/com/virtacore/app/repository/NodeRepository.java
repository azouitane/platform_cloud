package com.virtacore.app.repository;


import com.virtacore.app.entity.vm.Node;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NodeRepository extends JpaRepository<Node, UUID> {


    List<Node> findByClusterId(UUID clusterId);


    List<Node> findByClusterIdAndActiveTrue(
            UUID clusterId
    );


    Optional<Node> findByName(String name);


    Optional<Node> findByHost(String host);

    boolean existsByName(String name);

    boolean existsByHost(String host);

}
